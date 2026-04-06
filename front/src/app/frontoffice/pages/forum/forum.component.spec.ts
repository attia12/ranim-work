import { ComponentFixture, TestBed } from '@angular/core/testing';
import { PLATFORM_ID } from '@angular/core';
import { of, Subject } from 'rxjs';

import { ForumComponent } from './forum.component';
import { ForumService, ForumCategory, ForumPost, ForumComment } from '../../../services/forum.service';
import { AuthService } from '../../../services/auth.service';

// ── HELPERS ────────────────────────────────────────────────────────────────

function makeCategory(override: Partial<ForumCategory> = {}): ForumCategory {
  return { categoryId: 1, name: 'Angular', description: 'Angular topics', ...override };
}

function makePost(override: Partial<ForumPost> = {}): ForumPost {
  return {
    postId: 1, title: 'Hello', content: 'World',
    categoryName: 'Angular', authorName: 'Alice',
    totalComments: 0, totalLikes: 0,
    userId: 10, categoryId: 1,
    ...override
  };
}

function makeComment(override: Partial<ForumComment> = {}): ForumComment {
  return {
    commentId: 1, content: 'Nice!',
    authorName: 'Bob', timeComment: '2024-01-01T10:00:00',
    userId: 20, ...override
  };
}

// ── MOCKS ──────────────────────────────────────────────────────────────────

const currentUser$ = new Subject<any>();

const mockAuthService = {
  getCurrentUser: jasmine.createSpy('getCurrentUser').and.returnValue({
    id: 10, firstName: 'Alice'
  }),
  currentUser$: currentUser$.asObservable()
};

const mockForumService = {
  getAllCategories:   jasmine.createSpy('getAllCategories').and.returnValue(of([])),
  getPostsByCategory: jasmine.createSpy('getPostsByCategory').and.returnValue(of([])),
  getLikesByPost:    jasmine.createSpy('getLikesByPost').and.returnValue(of([])),
  createPost:        jasmine.createSpy('createPost').and.returnValue(of(makePost())),
  updatePost:        jasmine.createSpy('updatePost').and.returnValue(of(makePost())),
  deletePost:        jasmine.createSpy('deletePost').and.returnValue(of('deleted')),
  likePost:          jasmine.createSpy('likePost').and.returnValue(of({})),
  unlikePost:        jasmine.createSpy('unlikePost').and.returnValue(of('unliked')),
  getCommentsByPost: jasmine.createSpy('getCommentsByPost').and.returnValue(of([])),
  addComment:        jasmine.createSpy('addComment').and.returnValue(of(makeComment())),
  deleteComment:     jasmine.createSpy('deleteComment').and.returnValue(of('deleted')),
};

// ── SUITE ──────────────────────────────────────────────────────────────────

describe('ForumComponent', () => {
  let component: ForumComponent;
  let fixture: ComponentFixture<ForumComponent>;

  beforeEach(async () => {
    // Reset spies entre chaque test
    mockForumService.getAllCategories.calls.reset();
    mockForumService.getPostsByCategory.calls.reset();
    mockForumService.getLikesByPost.calls.reset();
    mockForumService.createPost.calls.reset();
    mockForumService.updatePost.calls.reset();
    mockForumService.deletePost.calls.reset();
    mockForumService.likePost.calls.reset();
    mockForumService.unlikePost.calls.reset();
    mockForumService.getCommentsByPost.calls.reset();
    mockForumService.addComment.calls.reset();
    mockForumService.deleteComment.calls.reset();

    await TestBed.configureTestingModule({
      declarations: [ForumComponent],
      providers: [
        { provide: ForumService, useValue: mockForumService },
        { provide: AuthService,  useValue: mockAuthService  },
        { provide: PLATFORM_ID,  useValue: 'browser'        }  // simule le browser
      ]
    }).compileComponents();

    fixture   = TestBed.createComponent(ForumComponent);
    component = fixture.componentInstance;
    fixture.detectChanges(); // déclenche ngOnInit
  });

  // ══════════════════════════════════════════════
  // Initialisation
  // ══════════════════════════════════════════════
  describe('ngOnInit()', () => {
    it('should create the component', () => {
      expect(component).toBeTruthy();
    });

    it('should load current user on init', () => {
      expect(component.currentUserId).toBe(10);
      expect(component.currentUserName).toBe('Alice');
      expect(component.isLoggedIn).toBeTrue();
    });

    it('should call getAllCategories on init', () => {
      expect(mockForumService.getAllCategories).toHaveBeenCalledTimes(1);
    });

    it('should populate categories after init', () => {
      const cats = [makeCategory(), makeCategory({ categoryId: 2, name: 'Java' })];
      mockForumService.getAllCategories.and.returnValue(of(cats));

      component.ngOnInit();

      expect(component.categories.length).toBe(2);
      expect(component.categories[0].name).toBe('Angular');
    });

    it('should set isLoggedIn to false when currentUser$ emits null', () => {
      currentUser$.next(null);
      expect(component.isLoggedIn).toBeFalse();
    });

    it('should update user info when currentUser$ emits a new user', () => {
      currentUser$.next({ id: 99, firstName: 'Charlie' });
      expect(component.currentUserId).toBe(99);
      expect(component.currentUserName).toBe('Charlie');
      expect(component.isLoggedIn).toBeTrue();
    });

    it('should NOT call getAllCategories when platform is server (SSR)', async () => {
      mockForumService.getAllCategories.calls.reset();

      await TestBed.resetTestingModule();
      await TestBed.configureTestingModule({
        declarations: [ForumComponent],
        providers: [
          { provide: ForumService, useValue: mockForumService },
          { provide: AuthService,  useValue: mockAuthService  },
          { provide: PLATFORM_ID,  useValue: 'server'         } // SSR
        ]
      }).compileComponents();

      const f = TestBed.createComponent(ForumComponent);
      f.detectChanges();

      expect(mockForumService.getAllCategories).not.toHaveBeenCalled();
    });
  });

  // ══════════════════════════════════════════════
  // Navigation catégories / posts
  // ══════════════════════════════════════════════
  describe('selectCategory()', () => {
    it('should set selectedCategory and call loadPosts', () => {
      const cat = makeCategory();
      mockForumService.getPostsByCategory.and.returnValue(of([]));

      component.selectCategory(cat);

      expect(component.selectedCategory).toEqual(cat);
      expect(mockForumService.getPostsByCategory).toHaveBeenCalledWith(cat.categoryId);
    });

    it('should populate posts after selecting a category', () => {
      const posts = [makePost(), makePost({ postId: 2, title: 'Second' })];
      mockForumService.getPostsByCategory.and.returnValue(of(posts));
      mockForumService.getLikesByPost.and.returnValue(of([]));

      component.selectCategory(makeCategory());

      expect(component.posts.length).toBe(2);
    });

    it('should call loadLikes for each post after loading', () => {
      const posts = [makePost({ postId: 1 }), makePost({ postId: 2 })];
      mockForumService.getPostsByCategory.and.returnValue(of(posts));
      mockForumService.getLikesByPost.and.returnValue(of([]));

      component.selectCategory(makeCategory());

      expect(mockForumService.getLikesByPost).toHaveBeenCalledWith(1);
      expect(mockForumService.getLikesByPost).toHaveBeenCalledWith(2);
    });
  });

  describe('backToCategories()', () => {
    it('should reset navigation state', () => {
      component.selectedCategory = makeCategory();
      component.posts = [makePost()];
      component.showNewPostForm = true;
      component.commentsMap.set(1, [makeComment()]);
      component.showCommentsMap.set(1, true);

      component.backToCategories();

      expect(component.selectedCategory).toBeNull();
      expect(component.posts).toEqual([]);
      expect(component.showNewPostForm).toBeFalse();
      expect(component.commentsMap.size).toBe(0);
      expect(component.showCommentsMap.size).toBe(0);
    });
  });

  // ══════════════════════════════════════════════
  // POSTS
  // ══════════════════════════════════════════════
  describe('createPost()', () => {
    beforeEach(() => {
      component.isLoggedIn = true;
      component.selectedCategory = makeCategory();
      component.currentUserId = 10;
      component.newPostTitle = 'New Title';
      component.newPostContent = 'New Content';
    });

    it('should call forumService.createPost with correct payload', () => {
      const newPost = makePost({ postId: 99, title: 'New Title' });
      mockForumService.createPost.and.returnValue(of(newPost));

      component.createPost();

      expect(mockForumService.createPost).toHaveBeenCalledWith({
        title: 'New Title',
        content: 'New Content',
        categoryId: 1,
        userId: 10
      });
    });

    it('should prepend new post to posts list', () => {
      component.posts = [makePost({ postId: 1 })];
      const newPost = makePost({ postId: 99 });
      mockForumService.createPost.and.returnValue(of(newPost));

      component.createPost();

      expect(component.posts[0].postId).toBe(99);
      expect(component.posts.length).toBe(2);
    });

    it('should reset form after creating post', () => {
      mockForumService.createPost.and.returnValue(of(makePost()));

      component.createPost();

      expect(component.newPostTitle).toBe('');
      expect(component.newPostContent).toBe('');
      expect(component.showNewPostForm).toBeFalse();
    });

    it('should NOT create post when not logged in', () => {
      component.isLoggedIn = false;
      spyOn(window, 'alert');

      component.createPost();

      expect(mockForumService.createPost).not.toHaveBeenCalled();
      expect(window.alert).toHaveBeenCalledWith('Please login to create a post!');
    });

    it('should NOT create post when title is empty', () => {
      component.newPostTitle = '   ';

      component.createPost();

      expect(mockForumService.createPost).not.toHaveBeenCalled();
    });

    it('should NOT create post when content is empty', () => {
      component.newPostContent = '   ';

      component.createPost();

      expect(mockForumService.createPost).not.toHaveBeenCalled();
    });
  });

  describe('startEditPost() / cancelEditPost()', () => {
    it('should set editing fields when startEditPost is called', () => {
      const post = makePost({ postId: 5, title: 'Old Title', content: 'Old Content' });

      component.startEditPost(post);

      expect(component.editingPostId).toBe(5);
      expect(component.editingPostTitle).toBe('Old Title');
      expect(component.editingPostContent).toBe('Old Content');
    });

    it('should reset editingPostId when cancelEditPost is called', () => {
      component.editingPostId = 5;

      component.cancelEditPost();

      expect(component.editingPostId).toBeNull();
    });
  });

  describe('saveEditPost()', () => {
    beforeEach(() => {
      component.selectedCategory = makeCategory();
      component.currentUserId = 10;
      component.posts = [makePost({ postId: 1, title: 'Old', content: 'Old content' })];
      component.editingPostId = 1;
      component.editingPostTitle = 'Updated Title';
      component.editingPostContent = 'Updated Content';
    });

    it('should call forumService.updatePost with correct payload', () => {
      mockForumService.updatePost.and.returnValue(of(makePost()));

      component.saveEditPost(component.posts[0]);

      expect(mockForumService.updatePost).toHaveBeenCalledWith(1, {
        title: 'Updated Title',
        content: 'Updated Content',
        categoryId: 1,
        userId: 10
      });
    });

    it('should update post in the list after saving', () => {
      mockForumService.updatePost.and.returnValue(of(makePost()));

      component.saveEditPost(component.posts[0]);

      expect(component.posts[0].title).toBe('Updated Title');
      expect(component.posts[0].content).toBe('Updated Content');
      expect(component.editingPostId).toBeNull();
    });

    it('should NOT save when editingPostTitle is empty', () => {
      component.editingPostTitle = '   ';

      component.saveEditPost(component.posts[0]);

      expect(mockForumService.updatePost).not.toHaveBeenCalled();
    });

    it('should NOT save when editingPostContent is empty', () => {
      component.editingPostContent = '';

      component.saveEditPost(component.posts[0]);

      expect(mockForumService.updatePost).not.toHaveBeenCalled();
    });
  });

  describe('deletePost()', () => {
    it('should call forumService.deletePost and remove post from list', () => {
      component.posts = [makePost({ postId: 1 }), makePost({ postId: 2 })];
      mockForumService.deletePost.and.returnValue(of('deleted'));

      component.deletePost(1);

      expect(mockForumService.deletePost).toHaveBeenCalledWith(1);
      expect(component.posts.length).toBe(1);
      expect(component.posts[0].postId).toBe(2);
    });
  });

  // ══════════════════════════════════════════════
  // LIKES
  // ══════════════════════════════════════════════
  describe('loadLikes()', () => {
    it('should mark post as liked when currentUser is in likes list', () => {
      const likes = [{ userId: 10 }, { userId: 20 }];
      mockForumService.getLikesByPost.and.returnValue(of(likes));
      component.posts = [makePost({ postId: 1 })];
      component.currentUserId = 10;

      component.loadLikes(1);

      expect(component.likedPostsMap.get(1)).toBeTrue();
      expect(component.posts[0].totalLikes).toBe(2);
    });

    it('should mark post as not liked when currentUser is not in likes list', () => {
      mockForumService.getLikesByPost.and.returnValue(of([{ userId: 99 }]));
      component.posts = [makePost({ postId: 1 })];
      component.currentUserId = 10;

      component.loadLikes(1);

      expect(component.likedPostsMap.get(1)).toBeFalse();
    });
  });

  describe('toggleLike()', () => {
    it('should call unlikePost and decrement totalLikes when post is liked', () => {
      const post = makePost({ postId: 1, totalLikes: 3 });
      component.isLoggedIn = true;
      component.currentUserId = 10;
      component.likedPostsMap.set(1, true);
      mockForumService.unlikePost.and.returnValue(of('unliked'));

      component.toggleLike(post);

      expect(mockForumService.unlikePost).toHaveBeenCalledWith(10, 1);
      expect(component.likedPostsMap.get(1)).toBeFalse();
      expect(post.totalLikes).toBe(2);
    });

    it('should call likePost and increment totalLikes when post is not liked', () => {
      const post = makePost({ postId: 1, totalLikes: 0 });
      component.isLoggedIn = true;
      component.currentUserId = 10;
      component.likedPostsMap.set(1, false);
      mockForumService.likePost.and.returnValue(of({}));

      component.toggleLike(post);

      expect(mockForumService.likePost).toHaveBeenCalledWith({ userId: 10, postId: 1 });
      expect(component.likedPostsMap.get(1)).toBeTrue();
      expect(post.totalLikes).toBe(1);
    });

    it('should NOT toggle like when not logged in', () => {
      component.isLoggedIn = false;
      spyOn(window, 'alert');

      component.toggleLike(makePost());

      expect(mockForumService.likePost).not.toHaveBeenCalled();
      expect(mockForumService.unlikePost).not.toHaveBeenCalled();
      expect(window.alert).toHaveBeenCalledWith('Please login to like a post!');
    });
  });

  describe('isLiked()', () => {
    it('should return true when post is liked', () => {
      component.likedPostsMap.set(1, true);
      expect(component.isLiked(1)).toBeTrue();
    });

    it('should return false when post is not liked', () => {
      component.likedPostsMap.set(1, false);
      expect(component.isLiked(1)).toBeFalse();
    });

    it('should return false when postId is not in map', () => {
      expect(component.isLiked(999)).toBeFalse();
    });
  });

  // ══════════════════════════════════════════════
  // COMMENTS
  // ══════════════════════════════════════════════
  describe('toggleComments()', () => {
    it('should load and show comments when they are hidden', () => {
      const post = makePost({ postId: 1 });
      const comments = [makeComment(), makeComment({ commentId: 2 })];
      mockForumService.getCommentsByPost.and.returnValue(of(comments));

      component.toggleComments(post);

      expect(mockForumService.getCommentsByPost).toHaveBeenCalledWith(1);
      expect(component.commentsMap.get(1)).toEqual(comments);
      expect(component.showCommentsMap.get(1)).toBeTrue();
    });

    it('should hide comments when they are already shown', () => {
      const post = makePost({ postId: 1 });
      component.showCommentsMap.set(1, true);

      component.toggleComments(post);

      expect(mockForumService.getCommentsByPost).not.toHaveBeenCalled();
      expect(component.showCommentsMap.get(1)).toBeFalse();
    });
  });

  describe('getComments()', () => {
    it('should return comments for a given postId', () => {
      const comments = [makeComment()];
      component.commentsMap.set(1, comments);
      expect(component.getComments(1)).toEqual(comments);
    });

    it('should return empty array when postId not in map', () => {
      expect(component.getComments(999)).toEqual([]);
    });
  });

  describe('isCommentsShown()', () => {
    it('should return true when comments are shown', () => {
      component.showCommentsMap.set(1, true);
      expect(component.isCommentsShown(1)).toBeTrue();
    });

    it('should return false when comments are hidden', () => {
      component.showCommentsMap.set(1, false);
      expect(component.isCommentsShown(1)).toBeFalse();
    });

    it('should return false when postId not in map', () => {
      expect(component.isCommentsShown(999)).toBeFalse();
    });
  });

  describe('getNewComment() / setNewComment()', () => {
    it('should set and get a comment value for a postId', () => {
      component.setNewComment(1, 'Hello comment');
      expect(component.getNewComment(1)).toBe('Hello comment');
    });

    it('should return empty string when postId not in map', () => {
      expect(component.getNewComment(999)).toBe('');
    });
  });

  describe('addComment()', () => {
    beforeEach(() => {
      component.isLoggedIn = true;
      component.currentUserId = 10;
      component.commentsMap.set(1, []);
      component.newCommentMap.set(1, 'Great post!');
    });

    it('should call forumService.addComment with correct payload', () => {
      const post = makePost({ postId: 1 });
      const newComment = makeComment({ commentId: 5, content: 'Great post!' });
      mockForumService.addComment.and.returnValue(of(newComment));

      component.addComment(post);

      expect(mockForumService.addComment).toHaveBeenCalledWith({
        content: 'Great post!',
        postId: 1,
        userId: 10
      });
    });

    it('should append comment to commentsMap and increment totalComments', () => {
      const post = makePost({ postId: 1, totalComments: 0 });
      const newComment = makeComment({ commentId: 5 });
      mockForumService.addComment.and.returnValue(of(newComment));

      component.addComment(post);

      expect(component.commentsMap.get(1)!.length).toBe(1);
      expect(post.totalComments).toBe(1);
    });

    it('should reset newCommentMap after adding comment', () => {
      const post = makePost({ postId: 1 });
      mockForumService.addComment.and.returnValue(of(makeComment()));

      component.addComment(post);

      expect(component.getNewComment(1)).toBe('');
    });

    it('should NOT add comment when not logged in', () => {
      component.isLoggedIn = false;
      spyOn(window, 'alert');

      component.addComment(makePost({ postId: 1 }));

      expect(mockForumService.addComment).not.toHaveBeenCalled();
      expect(window.alert).toHaveBeenCalledWith('Please login to comment!');
    });

    it('should NOT add comment when content is empty', () => {
      component.newCommentMap.set(1, '   ');

      component.addComment(makePost({ postId: 1 }));

      expect(mockForumService.addComment).not.toHaveBeenCalled();
    });
  });

  describe('deleteComment()', () => {
    it('should call forumService.deleteComment and remove comment from map', () => {
      const post = makePost({ postId: 1, totalComments: 2 });
      component.commentsMap.set(1, [
        makeComment({ commentId: 1 }),
        makeComment({ commentId: 2 })
      ]);
      mockForumService.deleteComment.and.returnValue(of('deleted'));

      component.deleteComment(post, 1);

      expect(mockForumService.deleteComment).toHaveBeenCalledWith(1);
      expect(component.commentsMap.get(1)!.length).toBe(1);
      expect(component.commentsMap.get(1)![0].commentId).toBe(2);
      expect(post.totalComments).toBe(1);
    });
  });
});