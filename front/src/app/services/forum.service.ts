import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface ForumCategory {
  categoryId: number;
  name: string;
  description: string;
}

export interface ForumPost {
  postId: number;
  title: string;
  content: string;
  categoryName: string;
  authorName: string;
  totalComments: number;
  totalLikes: number;
  userId?: number;
  categoryId?: number;
}

export interface ForumComment {
  commentId: number;
  content: string;
  authorName: string;
  timeComment: string;
  userId?: number;
}

@Injectable({
  providedIn: 'root'
})
export class ForumService {
  private apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  getAllCategories(): Observable<ForumCategory[]> {
    return this.http.get<ForumCategory[]>(`${this.apiUrl}/categories`);
  }

  createCategory(data: { name: string; description: string }): Observable<ForumCategory> {
    return this.http.post<ForumCategory>(`${this.apiUrl}/categories`, data);
  }

  deleteCategory(id: number): Observable<string> {
    return this.http.delete(`${this.apiUrl}/categories/${id}`, { responseType: 'text' });
  }

  getAllPosts(): Observable<ForumPost[]> {
    return this.http.get<ForumPost[]>(`${this.apiUrl}/posts`);
  }

  getPostsByCategory(categoryId: number): Observable<ForumPost[]> {
    return this.http.get<ForumPost[]>(`${this.apiUrl}/posts/category/${categoryId}`);
  }

  createPost(data: { title: string; content: string; categoryId: number; userId: number }): Observable<ForumPost> {
    return this.http.post<ForumPost>(`${this.apiUrl}/posts`, data);
  }

  updatePost(postId: number, data: Partial<ForumPost>): Observable<ForumPost> {
    return this.http.put<ForumPost>(`${this.apiUrl}/posts/${postId}`, data);
  }

  deletePost(postId: number): Observable<string> {
    return this.http.delete(`${this.apiUrl}/posts/${postId}`, { responseType: 'text' });
  }

  getCommentsByPost(postId: number): Observable<ForumComment[]> {
    return this.http.get<ForumComment[]>(`${this.apiUrl}/comments/post/${postId}`);
  }

  addComment(data: { content: string; postId: number; userId: number }): Observable<ForumComment> {
    return this.http.post<ForumComment>(`${this.apiUrl}/comments`, data);
  }

  deleteComment(commentId: number): Observable<string> {
    return this.http.delete(`${this.apiUrl}/comments/${commentId}`, { responseType: 'text' });
  }

  likePost(data: { userId: number; postId: number }): Observable<unknown> {
    return this.http.post(`${this.apiUrl}/likes`, data);
  }

  unlikePost(userId: number, postId: number): Observable<string> {
    return this.http.delete(`${this.apiUrl}/likes/${userId}/${postId}`, { responseType: 'text' });
  }

  getLikesByPost(postId: number): Observable<unknown[]> {
    return this.http.get<unknown[]>(`${this.apiUrl}/likes/post/${postId}`);
  }
}
