import { Injectable, Inject, NgZone, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { BehaviorSubject } from 'rxjs';
import { RxStomp } from '@stomp/rx-stomp';
import SockJS from 'sockjs-client';
import { environment } from '../../environments/environment';

export type NotifType = 'delivery' | 'cart' | 'order' | 'info';

export interface AppNotification {
  id: string;
  type: NotifType;
  title: string;
  message: string;
  time: Date;
  read: boolean;
  icon: string;
  iconColor: string;
  link?: string;
}

@Injectable({ providedIn: 'root' })
export class NotificationService {
  // Key is null until a user logs in — prevents leaking between users
  private userId: string | null = null;
  private notifs = new BehaviorSubject<AppNotification[]>([]);
  public notifications$ = this.notifs.asObservable();

  private rxStomp: RxStomp | null = null;

  constructor(
    @Inject(PLATFORM_ID) private platformId: Object,
    private ngZone: NgZone
  ) {}

  /** Called when a user logs in — loads their personal notifications and opens WebSocket */
  initForUser(userId: string): void {
    this.userId = userId;
    this.notifs.next(this.load());
    if (isPlatformBrowser(this.platformId)) {
      this.connectWs();
    }
  }

  /** Called on logout — clears in-memory state and closes WebSocket */
  clearSession(): void {
    this.userId = null;
    this.notifs.next([]);
    this.disconnectWs();
  }

  private connectWs(): void {
    const token = localStorage.getItem('token');
    if (!token) { console.warn('[WS] No token found, skipping connection'); return; }

    console.log('[WS] connectWs() called, disconnecting previous if any');
    this.disconnectWs();

    const stomp = new RxStomp();
    stomp.configure({
      webSocketFactory: () => {
        console.log('[WS] Creating SockJS to', `${environment.apiUrl}/ws`);
        return new SockJS(`${environment.apiUrl}/ws`);
      },
      connectHeaders: { Authorization: `Bearer ${token}` },
      heartbeatIncoming: 10000,
      heartbeatOutgoing: 10000,
      reconnectDelay: 5000,
      debug: (msg) => console.log('[STOMP]', msg)
    });
    stomp.activate();
    console.log('[WS] stomp.activate() called');

    stomp.connected$.subscribe(state => {
      console.log('[WS] connected$ state:', state);
    });

    stomp.watch(`/topic/notif-${this.userId}`).subscribe({
      next: (frame) => {
        console.log('[WS] MESSAGE received on /topic/notif-' + this.userId + ':', frame.body);
        try {
          const payload = JSON.parse(frame.body);
          this.ngZone.run(() => {
            console.log('[WS] Running push() inside NgZone');
            this.push(this.wsPayloadToNotif(payload));
          });
        } catch (e) {
          console.error('[WS] Failed to parse frame body:', e);
        }
      },
      error: (err) => console.error('[WS] watch() error:', err),
      complete: () => console.warn('[WS] watch() completed (subscription ended)')
    });
    console.log('[WS] Subscribed to /topic/notif-' + this.userId);

    this.rxStomp = stomp;
  }

  private disconnectWs(): void {
    if (this.rxStomp) {
      this.rxStomp.deactivate();
      this.rxStomp = null;
    }
  }

  private wsPayloadToNotif(payload: any): Omit<AppNotification, 'id' | 'time' | 'read'> {
    const type = payload.type as string;
    switch (type) {
      case 'CAMPSITE_BOOKING_CONFIRMED':
        return { type: 'order', title: 'Booking Confirmed', message: payload.message, icon: 'fa-check-circle', iconColor: '#16a34a', link: '/my-bookings' };
      case 'CAMPSITE_BOOKING_CANCELLED':
        return { type: 'info', title: 'Booking Cancelled', message: payload.message, icon: 'fa-times-circle', iconColor: '#dc2626', link: '/my-bookings' };
      case 'OUTDOOR_PROPOSAL_APPROVED':
        return { type: 'order', title: 'Proposal Approved', message: payload.message, icon: 'fa-check-circle', iconColor: '#16a34a', link: '/my-proposals' };
      case 'OUTDOOR_PROPOSAL_REJECTED':
        return { type: 'info', title: 'Proposal Rejected', message: payload.message, icon: 'fa-times-circle', iconColor: '#dc2626', link: '/my-proposals' };
      case 'OUTDOOR_BOOKING_CANCELLED':
        return { type: 'info', title: 'Outdoor Booking Cancelled', message: payload.message, icon: 'fa-times-circle', iconColor: '#dc2626', link: '/my-bookings' };
      default:
        return { type: 'info', title: 'Notification', message: payload.message || type, icon: 'fa-bell', iconColor: '#6b7280' };
    }
  }

  private storageKey(): string {
    return this.userId ? `campway_notifs_${this.userId}` : 'campway_notifs_guest';
  }

  private load(): AppNotification[] {
    if (isPlatformBrowser(this.platformId)) {
      try {
        const raw = localStorage.getItem(this.storageKey());
        if (raw) {
          return JSON.parse(raw).map((n: any) => ({ ...n, time: new Date(n.time) }));
        }
      } catch {}
    }
    return [];
  }

  private save(list: AppNotification[]): void {
    if (isPlatformBrowser(this.platformId)) {
      try { localStorage.setItem(this.storageKey(), JSON.stringify(list)); } catch {}
    }
  }

  push(n: Omit<AppNotification, 'id' | 'time' | 'read'>): void {
    const next: AppNotification = {
      ...n,
      id: Date.now().toString(),
      time: new Date(),
      read: false
    };
    const updated = [next, ...this.notifs.getValue()].slice(0, 50);
    this.notifs.next(updated);
    this.save(updated);
  }

  markAllRead(): void {
    const updated = this.notifs.getValue().map(n => ({ ...n, read: true }));
    this.notifs.next(updated);
    this.save(updated);
  }

  markRead(id: string): void {
    const updated = this.notifs.getValue().map(n => n.id === id ? { ...n, read: true } : n);
    this.notifs.next(updated);
    this.save(updated);
  }

  clear(): void {
    this.notifs.next([]);
    this.save([]);
  }

  getUnreadCount(): number {
    return this.notifs.getValue().filter(n => !n.read).length;
  }
}
