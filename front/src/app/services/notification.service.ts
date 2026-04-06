import { Injectable, Inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { BehaviorSubject } from 'rxjs';

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

  constructor(@Inject(PLATFORM_ID) private platformId: Object) {}

  /** Called when a user logs in — loads their personal notifications */
  initForUser(userId: string): void {
    this.userId = userId;
    this.notifs.next(this.load());
  }

  /** Called on logout — clears in-memory state but keeps localStorage for next login */
  clearSession(): void {
    this.userId = null;
    this.notifs.next([]);
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
