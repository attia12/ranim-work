import { TestBed } from '@angular/core/testing';
import { NotificationService, AppNotification } from './notification.service';
import { PLATFORM_ID } from '@angular/core';

const mockNotif = {
  type: 'cart' as const,
  title: 'Added to Cart',
  message: 'Tent was added to your cart',
  icon: 'fa-cart',
  iconColor: '#fff'
};

describe('NotificationService', () => {
  let service: NotificationService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        NotificationService,
        { provide: PLATFORM_ID, useValue: 'browser' }
      ]
    });
    service = TestBed.inject(NotificationService);
    localStorage.clear();
  });

  afterEach(() => localStorage.clear());

  // ── push ───────────────────────────────────────────────────────────────────

  it('should add a notification', () => {
    service.push(mockNotif);
    let count = 0;
    service.notifications$.subscribe(n => count = n.length);
    expect(count).toBe(1);
  });

  it('should prepend new notifications to the list', () => {
    service.push({ ...mockNotif, title: 'First' });
    service.push({ ...mockNotif, title: 'Second' });
    let notifs: AppNotification[] = [];
    service.notifications$.subscribe(n => notifs = n);
    expect(notifs[0].title).toBe('Second');
    expect(notifs[1].title).toBe('First');
  });

  it('should cap notifications at 50', () => {
    for (let i = 0; i < 55; i++) {
      service.push({ ...mockNotif, title: `Notif ${i}` });
    }
    let count = 0;
    service.notifications$.subscribe(n => count = n.length);
    expect(count).toBe(50);
  });

  it('should set read to false on new notification', () => {
    service.push(mockNotif);
    let notifs: AppNotification[] = [];
    service.notifications$.subscribe(n => notifs = n);
    expect(notifs[0].read).toBeFalse();
  });

  // ── markRead ───────────────────────────────────────────────────────────────

  it('should mark a single notification as read', () => {
    service.push(mockNotif);
    let notifs: AppNotification[] = [];
    service.notifications$.subscribe(n => notifs = n);
    const id = notifs[0].id;
    service.markRead(id);
    service.notifications$.subscribe(n => notifs = n);
    expect(notifs[0].read).toBeTrue();
  });

  it('should not affect other notifications when marking one read', () => {
    spyOn(Date, 'now').and.returnValues(1000, 2000);
    service.push({ ...mockNotif, title: 'First' });
    service.push({ ...mockNotif, title: 'Second' });
    let notifs: AppNotification[] = [];
    service.notifications$.subscribe(n => notifs = n);
    service.markRead(notifs[0].id);
    service.notifications$.subscribe(n => notifs = n);
    expect(notifs[1].read).toBeFalse();
  });

  // ── markAllRead ────────────────────────────────────────────────────────────

  it('should mark all notifications as read', () => {
    service.push(mockNotif);
    service.push(mockNotif);
    service.markAllRead();
    let notifs: AppNotification[] = [];
    service.notifications$.subscribe(n => notifs = n);
    expect(notifs.every(n => n.read)).toBeTrue();
  });

  // ── getUnreadCount ─────────────────────────────────────────────────────────

  it('should return 0 unread when no notifications', () => {
    expect(service.getUnreadCount()).toBe(0);
  });

  it('should return correct unread count', () => {
    service.push(mockNotif);
    service.push(mockNotif);
    expect(service.getUnreadCount()).toBe(2);
  });

  it('should decrease unread count after markAllRead', () => {
    service.push(mockNotif);
    service.push(mockNotif);
    service.markAllRead();
    expect(service.getUnreadCount()).toBe(0);
  });

  // ── clear ──────────────────────────────────────────────────────────────────

  it('should clear all notifications', () => {
    service.push(mockNotif);
    service.clear();
    let count = 0;
    service.notifications$.subscribe(n => count = n.length);
    expect(count).toBe(0);
  });

  // ── clearSession ───────────────────────────────────────────────────────────

  it('should clear notifications on clearSession', () => {
    service.initForUser('user1');
    service.push(mockNotif);
    service.clearSession();
    let count = 0;
    service.notifications$.subscribe(n => count = n.length);
    expect(count).toBe(0);
  });

  // ── initForUser ────────────────────────────────────────────────────────────

  it('should load empty list for a new user', () => {
    service.initForUser('new-user-id');
    let count = 0;
    service.notifications$.subscribe(n => count = n.length);
    expect(count).toBe(0);
  });
});
