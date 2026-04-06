import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface Message {
  messageId: number;
  content: string;
  sentAt: Date;
  date: Date;
  isRead: boolean;
  senderId: number;
  receiverId: number;
  senderName: string;
  receiverName: string;
}

@Injectable({
  providedIn: 'root'
})
export class MessagingService {
  private apiUrl = environment.apiUrl + '/messages';

  constructor(private http: HttpClient) {}

  sendMessage(data: { senderId: number; receiverId: number; content: string }): Observable<Message> {
    return this.http.post<Message>(this.apiUrl, data);
  }

  getConversation(senderId: number, receiverId: number): Observable<Message[]> {
    return this.http.get<Message[]>(`${this.apiUrl}/conversation/${senderId}/${receiverId}`);
  }

  getReceivedMessages(userId: number): Observable<Message[]> {
    return this.http.get<Message[]>(`${this.apiUrl}/received/${userId}`);
  }

  getSentMessages(userId: number): Observable<Message[]> {
    return this.http.get<Message[]>(`${this.apiUrl}/sent/${userId}`);
  }

  getUnreadMessages(userId: number): Observable<Message[]> {
    return this.http.get<Message[]>(`${this.apiUrl}/unread/${userId}`);
  }

  markAsRead(messageId: number): Observable<string> {
    return this.http.put(`${this.apiUrl}/read/${messageId}`, {}, { responseType: 'text' });
  }

  updateMessage(messageId: number, content: string): Observable<Message> {
    return this.http.patch<Message>(`${this.apiUrl}/${messageId}`, { content });
  }

  deleteMessage(messageId: number): Observable<string> {
    return this.http.delete(`${this.apiUrl}/${messageId}`, { responseType: 'text' });
  }
}
