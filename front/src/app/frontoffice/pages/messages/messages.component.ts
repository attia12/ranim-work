import { Component, OnInit, AfterViewChecked, ViewChild, ElementRef, ChangeDetectorRef } from '@angular/core';
import { MessagingService, Message } from '../../../services/messaging.service';
import { AuthService } from '../../../services/auth.service';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../environments/environment';
import { Subject } from 'rxjs';
import { debounceTime, distinctUntilChanged, switchMap } from 'rxjs/operators';

export interface ChatSession {
  id: string;
  participantId: string;
  participantName: string;
  lastMessage: string;
  lastMessageDate: Date;
  unreadCount: number;
}

@Component({
  selector: 'app-messages',
  templateUrl: './messages.component.html',
  styleUrl: './messages.component.css'
})
export class MessagesComponent implements OnInit, AfterViewChecked {

  @ViewChild('scrollMe') private scrollContainer!: ElementRef;

  sessions: ChatSession[] = [];
  selectedSession: ChatSession | null = null;
  messages: Message[] = [];
  newMessage: string = '';
  currentUserId: number = 0;
  conversationSearch = '';

  userSearchQuery = '';
  userSearchResults: any[] = [];
  isSearching = false;
  showSearch = false;

  editingMessageId: number | null = null;
  editingContent: string = '';

  // ── VALIDATION ─────────────────────────────────
  newMessageError: string = '';
  editMessageError: string = '';

  private searchSubject = new Subject<string>();
  private shouldScroll = false;

  constructor(
    private messagingService: MessagingService,
    private authService: AuthService,
    private http: HttpClient,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    const user = this.authService.getCurrentUser();
    const storedId = localStorage.getItem('userId');

    if (user?.id && Number(user.id) > 0) {
      this.currentUserId = Number(user.id);
    } else if (storedId && Number(storedId) > 0) {
      this.currentUserId = Number(storedId);
    }

    if (this.currentUserId > 0) {
      this.loadReceivedMessages();
    }

    this.searchSubject.pipe(
      debounceTime(300),
      distinctUntilChanged(),
      switchMap(q => {
        if (q.length < 2) {
          this.userSearchResults = [];
          this.isSearching = false;
          return [];
        }
        this.isSearching = true;
        return this.http.get<any[]>(
          `${environment.apiUrl}/auth/users/search?q=${encodeURIComponent(q)}`
        );
      })
    ).subscribe({
      next: (results: any[]) => {
        this.userSearchResults = results;
        this.isSearching = false;
      },
      error: () => {
        this.isSearching = false;
        this.userSearchResults = [];
      }
    });
  }

  ngAfterViewChecked() {
    if (this.shouldScroll) {
      this.scrollToBottom();
      this.shouldScroll = false;
    }
  }

  private scrollToBottom(): void {
    try {
      if (this.scrollContainer) {
        this.scrollContainer.nativeElement.scrollTop =
          this.scrollContainer.nativeElement.scrollHeight;
      }
    } catch (err) {}
  }

  get filteredSessions(): ChatSession[] {
    if (!this.conversationSearch.trim()) return this.sessions;
    return this.sessions.filter(s =>
      s.participantName.toLowerCase().includes(this.conversationSearch.toLowerCase())
    );
  }

  loadReceivedMessages() {
    this.messagingService.getReceivedMessages(this.currentUserId).subscribe({
      next: (messages: Message[]) => {
        const sessionMap = new Map<string, ChatSession>();
        messages.forEach((msg: any) => {
          const rawSenderId = msg.senderId ?? null;
          if (!rawSenderId) return;

          const participantId = rawSenderId.toString();
          const participantName = msg.senderName || 'Unknown';

          if (!sessionMap.has(participantId)) {
            sessionMap.set(participantId, {
              id: participantId,
              participantId: participantId,
              participantName: participantName,
              lastMessage: msg.content,
              lastMessageDate: new Date(msg.sentAt),
              unreadCount: msg.read ? 0 : 1
            });
          } else {
            const session = sessionMap.get(participantId)!;
            if (!msg.read) session.unreadCount++;
            session.lastMessage = msg.content;
            session.lastMessageDate = new Date(msg.sentAt);
          }
        });
        this.sessions = Array.from(sessionMap.values());
      },
      error: (err: any) => console.error('❌ Error loading messages:', err)
    });
  }

  onSearchInput() {
    this.searchSubject.next(this.userSearchQuery);
  }

  startConversation(targetUser: any) {
    const existing = this.sessions.find(
      s => s.participantId === targetUser.id.toString()
    );
    if (existing) {
      this.selectSession(existing);
    } else {
      const newSession: ChatSession = {
        id: targetUser.id.toString(),
        participantId: targetUser.id.toString(),
        participantName: targetUser.fullname || targetUser.email,
        lastMessage: '',
        lastMessageDate: new Date(),
        unreadCount: 0
      };
      this.sessions = [newSession, ...this.sessions];
      this.selectSession(newSession);
    }
    this.userSearchQuery = '';
    this.userSearchResults = [];
    this.showSearch = false;
  }

  selectSession(session: ChatSession) {
    this.selectedSession = session;
    this.editingMessageId = null;
    this.newMessageError = '';

    if (!session?.participantId) return;

    const receiverId = Number(session.participantId);
    if (!this.currentUserId || isNaN(receiverId) || receiverId === 0) return;

    this.messagingService.getConversation(
      this.currentUserId,
      receiverId
    ).subscribe({
      next: (msgs: Message[]) => {
        this.messages = msgs;
        this.shouldScroll = true;
        msgs.filter((m: any) => !m.read && m.senderId !== this.currentUserId)
          .forEach((m: any) => {
            this.messagingService.markAsRead(m.messageId).subscribe();
          });
        session.unreadCount = 0;
      },
      error: (err: any) => console.error('❌ Error loading conversation:', err)
    });
  }

  sendMessage() {
    this.newMessageError = '';

    if (!this.selectedSession) return;
    if (this.currentUserId === 0) return;

    // ── Validation ──
    if (!this.newMessage.trim()) {
      this.newMessageError = 'Message cannot be empty.';
      return;
    }
    if (this.newMessage.trim().length < 1) {
      this.newMessageError = 'Message is too short.';
      return;
    }
    if (this.newMessage.trim().length > 500) {
      this.newMessageError = 'Message must not exceed 500 characters.';
      return;
    }

    const receiverId = Number(this.selectedSession.participantId);
    if (isNaN(receiverId) || receiverId === 0) return;

    this.messagingService.sendMessage({
      senderId: this.currentUserId,
      receiverId: receiverId,
      content: this.newMessage
    }).subscribe({
      next: (msg: Message) => {
        this.messages.push(msg);
        this.shouldScroll = true;
        this.selectedSession!.lastMessage = this.newMessage;
        this.selectedSession!.lastMessageDate = new Date();
        this.newMessage = '';
        this.newMessageError = '';
      },
      error: (err: any) => console.error('❌ Error sending message:', err)
    });
  }

  startEdit(msg: any) {
    this.editingMessageId = msg.messageId;
    this.editingContent = msg.content;
    this.editMessageError = '';
  }

  cancelEdit() {
    this.editingMessageId = null;
    this.editingContent = '';
    this.editMessageError = '';
  }

  saveEdit(msg: any) {
    this.editMessageError = '';

    if (!this.editingContent.trim()) {
      this.editMessageError = 'Message cannot be empty.';
      return;
    }
    if (this.editingContent.trim().length > 500) {
      this.editMessageError = 'Message must not exceed 500 characters.';
      return;
    }

    this.messagingService.updateMessage(msg.messageId, this.editingContent).subscribe({
      next: () => {
        const index = this.messages.findIndex((m: any) => m.messageId === msg.messageId);
        if (index !== -1) {
          (this.messages[index] as any).content = this.editingContent;
        }
        this.editingMessageId = null;
        this.editingContent = '';
        this.editMessageError = '';
      },
      error: (err: any) => console.error('❌ Error updating message:', err)
    });
  }

  deleteMessage(messageId: number) {
    this.messagingService.deleteMessage(messageId).subscribe({
      next: () => {
        this.messages = this.messages.filter((m: any) => m.messageId !== messageId);
      },
      error: (err: any) => console.error('❌ Error deleting message:', err)
    });
  }

  isMine(msg: any): boolean {
    return Number(msg.senderId) === this.currentUserId;
  }
}
