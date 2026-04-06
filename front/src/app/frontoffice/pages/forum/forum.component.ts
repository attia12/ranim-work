import { Component, OnInit, Inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { ForumService, ForumCategory, ForumPost, ForumComment } from '../../../services/forum.service';
import { AuthService } from '../../../services/auth.service';

@Component({
  selector: 'app-forum',
  templateUrl: './forum.component.html',
  styleUrl: './forum.component.css'
})
export class ForumComponent implements OnInit {

  categories: ForumCategory[] = [];
  selectedCategory: ForumCategory | null = null;
  posts: ForumPost[] = [];
  currentUserId: number = 0;
  currentUserName: string = '';
  isLoggedIn: boolean = false;

  showNewPostForm = false;
  newPostTitle = '';
  newPostContent = '';

  commentsMap: Map<number, ForumComment[]> = new Map();
  showCommentsMap: Map<number, boolean> = new Map();
  newCommentMap: Map<number, string> = new Map();
  likedPostsMap: Map<number, boolean> = new Map();

  editingPostId: number | null = null;
  editingPostTitle: string = '';
  editingPostContent: string = '';

  // ── VALIDATION ─────────────────────────────────
  postTitleError: string = '';
  postContentError: string = '';
  editTitleError: string = '';
  editContentError: string = '';
  commentErrors: Map<number, string> = new Map();

  constructor(
    private forumService: ForumService,
    private authService: AuthService,
    @Inject(PLATFORM_ID) private platformId: Object
  ) {}

  ngOnInit() {
    if (isPlatformBrowser(this.platformId)) {
      const user = this.authService.getCurrentUser();
      if (user) {
        this.currentUserId = Number(user.id);
        this.currentUserName = user.firstName || '';
        this.isLoggedIn = true;
      }

      this.authService.currentUser$.subscribe(u => {
        if (u) {
          this.currentUserId = Number(u.id);
          this.currentUserName = u.firstName || '';
          this.isLoggedIn = true;
        } else {
          this.isLoggedIn = false;
        }
      });

      this.forumService.getAllCategories().subscribe({
        next: (cats: ForumCategory[]) => {
          this.categories = cats;
        },
        error: (err) => console.error('❌ Error loading categories:', err)
      });
    }
  }

  selectCategory(category: ForumCategory) {
    this.selectedCategory = category;
    this.loadPosts(category.categoryId);
  }

  loadPosts(categoryId: number) {
    this.forumService.getPostsByCategory(categoryId).subscribe((posts: ForumPost[]) => {
      this.posts = posts;
      posts.forEach(post => this.loadLikes(post.postId));
    });
  }

  backToCategories() {
    this.selectedCategory = null;
    this.posts = [];
    this.showNewPostForm = false;
    this.commentsMap.clear();
    this.showCommentsMap.clear();
  }

  // ── POSTS ──────────────────────────────────────

  createPost() {
    this.postTitleError = '';
    this.postContentError = '';

    if (!this.isLoggedIn) {
      alert('Please login to create a post!');
      return;
    }

    if (!this.newPostTitle.trim()) {
      this.postTitleError = 'Title is required.';
    } else if (this.newPostTitle.trim().length < 5) {
      this.postTitleError = 'Title must be at least 5 characters.';
    } else if (this.newPostTitle.trim().length > 100) {
      this.postTitleError = 'Title must not exceed 100 characters.';
    }

    if (!this.newPostContent.trim()) {
      this.postContentError = 'Content is required.';
    } else if (this.newPostContent.trim().length < 10) {
      this.postContentError = 'Content must be at least 10 characters.';
    } else if (this.newPostContent.trim().length > 1000) {
      this.postContentError = 'Content must not exceed 1000 characters.';
    }

    if (this.postTitleError || this.postContentError) return;

    this.forumService.createPost({
      title: this.newPostTitle,
      content: this.newPostContent,
      categoryId: this.selectedCategory!.categoryId,
      userId: this.currentUserId
    }).subscribe((post: ForumPost) => {
      this.posts.unshift(post);
      this.newPostTitle = '';
      this.newPostContent = '';
      this.showNewPostForm = false;
    });
  }

  startEditPost(post: ForumPost) {
    this.editingPostId = post.postId;
    this.editingPostTitle = post.title;
    this.editingPostContent = post.content;
    this.editTitleError = '';
    this.editContentError = '';
  }

  cancelEditPost() {
    this.editingPostId = null;
    this.editTitleError = '';
    this.editContentError = '';
  }

  saveEditPost(post: ForumPost) {
    this.editTitleError = '';
    this.editContentError = '';

    if (!this.editingPostTitle.trim()) {
      this.editTitleError = 'Title is required.';
    } else if (this.editingPostTitle.trim().length < 5) {
      this.editTitleError = 'Title must be at least 5 characters.';
    } else if (this.editingPostTitle.trim().length > 100) {
      this.editTitleError = 'Title must not exceed 100 characters.';
    }

    if (!this.editingPostContent.trim()) {
      this.editContentError = 'Content is required.';
    } else if (this.editingPostContent.trim().length < 10) {
      this.editContentError = 'Content must be at least 10 characters.';
    } else if (this.editingPostContent.trim().length > 1000) {
      this.editContentError = 'Content must not exceed 1000 characters.';
    }

    if (this.editTitleError || this.editContentError) return;

    this.forumService.updatePost(post.postId, {
      title: this.editingPostTitle,
      content: this.editingPostContent,
      categoryId: this.selectedCategory!.categoryId,
      userId: this.currentUserId
    }).subscribe(() => {
      const index = this.posts.findIndex(p => p.postId === post.postId);
      if (index !== -1) {
        this.posts[index].title = this.editingPostTitle;
        this.posts[index].content = this.editingPostContent;
      }
      this.editingPostId = null;
    });
  }

  deletePost(postId: number) {
    this.forumService.deletePost(postId).subscribe(() => {
      this.posts = this.posts.filter(p => p.postId !== postId);
    });
  }

  // ── LIKES ──────────────────────────────────────

  loadLikes(postId: number) {
    this.forumService.getLikesByPost(postId).subscribe((likes: any[]) => {
      const liked = likes.some(l => l.userId === this.currentUserId);
      this.likedPostsMap.set(postId, liked);
      const post = this.posts.find(p => p.postId === postId);
      if (post) post.totalLikes = likes.length;
    });
  }

  toggleLike(post: ForumPost) {
    if (!this.isLoggedIn) {
      alert('Please login to like a post!');
      return;
    }
    const liked = this.likedPostsMap.get(post.postId) || false;
    if (liked) {
      this.forumService.unlikePost(this.currentUserId, post.postId).subscribe(() => {
        this.likedPostsMap.set(post.postId, false);
        post.totalLikes--;
      });
    } else {
      this.forumService.likePost({
        userId: this.currentUserId,
        postId: post.postId
      }).subscribe(() => {
        this.likedPostsMap.set(post.postId, true);
        post.totalLikes++;
      });
    }
  }

  isLiked(postId: number): boolean {
    return this.likedPostsMap.get(postId) || false;
  }

  // ── COMMENTS ───────────────────────────────────

  toggleComments(post: ForumPost) {
    const isShown = this.showCommentsMap.get(post.postId) || false;
    if (!isShown) {
      this.forumService.getCommentsByPost(post.postId).subscribe((comments: ForumComment[]) => {
        this.commentsMap.set(post.postId, comments);
        this.showCommentsMap.set(post.postId, true);
      });
    } else {
      this.showCommentsMap.set(post.postId, false);
    }
  }

  getComments(postId: number): ForumComment[] {
    return this.commentsMap.get(postId) || [];
  }

  isCommentsShown(postId: number): boolean {
    return this.showCommentsMap.get(postId) || false;
  }

  getNewComment(postId: number): string {
    return this.newCommentMap.get(postId) || '';
  }

  setNewComment(postId: number, value: string) {
    this.newCommentMap.set(postId, value);
  }

  addComment(post: ForumPost) {
    this.commentErrors.set(post.postId, '');

    if (!this.isLoggedIn) {
      alert('Please login to comment!');
      return;
    }

    const content = this.getNewComment(post.postId).trim();

    if (!content) {
      this.commentErrors.set(post.postId, 'Comment is required.');
      return;
    } else if (content.length < 2) {
      this.commentErrors.set(post.postId, 'Comment must be at least 2 characters.');
      return;
    } else if (content.length > 500) {
      this.commentErrors.set(post.postId, 'Comment must not exceed 500 characters.');
      return;
    }

    this.forumService.addComment({
      content,
      postId: post.postId,
      userId: this.currentUserId
    }).subscribe((comment: ForumComment) => {
      const comments = this.commentsMap.get(post.postId) || [];
      comments.push(comment);
      this.commentsMap.set(post.postId, comments);
      this.newCommentMap.set(post.postId, '');
      this.commentErrors.set(post.postId, '');
      post.totalComments++;
    });
  }

  deleteComment(post: ForumPost, commentId: number) {
    this.forumService.deleteComment(commentId).subscribe(() => {
      const comments = this.commentsMap.get(post.postId) || [];
      this.commentsMap.set(
        post.postId,
        comments.filter(c => c.commentId !== commentId)
      );
      post.totalComments--;
    });
  }
}
