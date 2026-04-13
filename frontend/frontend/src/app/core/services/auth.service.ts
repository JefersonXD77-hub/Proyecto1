import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap, map, catchError, of } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AuthUser } from '../models/auth-user.model';

interface LoginResponse {
  status: string;
  message: string;
  user: AuthUser;
}

interface MeResponse {
  status: string;
  user: AuthUser;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly apiUrl = `${environment.apiUrl}/api/auth`;
  private currentUserSubject = new BehaviorSubject<AuthUser | null>(null);
  currentUser$ = this.currentUserSubject.asObservable();

  constructor(private http: HttpClient) {}

  login(username: string, password: string): Observable<AuthUser> {
    return this.http.post<LoginResponse>(`${this.apiUrl}/login`, { username, password }, {
      withCredentials: true
    }).pipe(
      map(response => response.user),
      tap(user => this.currentUserSubject.next(user))
    );
  }

  logout(): Observable<any> {
    return this.http.post(`${this.apiUrl}/logout`, {}, {
      withCredentials: true
    }).pipe(
      tap(() => this.currentUserSubject.next(null))
    );
  }

  loadSession(): Observable<AuthUser | null> {
    return this.http.get<MeResponse>(`${this.apiUrl}/me`, {
      withCredentials: true
    }).pipe(
      map(response => response.user),
      tap(user => this.currentUserSubject.next(user)),
      catchError(() => {
        this.currentUserSubject.next(null);
        return of(null);
      })
    );
  }

  getCurrentUser(): AuthUser | null {
    return this.currentUserSubject.value;
  }

  isLoggedIn(): boolean {
    return this.currentUserSubject.value !== null;
  }

  hasRole(...roles: string[]): boolean {
    const user = this.currentUserSubject.value;
    return !!user && roles.includes(user.rol);
  }
}