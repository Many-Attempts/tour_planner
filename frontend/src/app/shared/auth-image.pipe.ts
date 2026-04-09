import { HttpClient } from '@angular/common/http';
import { Pipe, PipeTransform, inject } from '@angular/core';
import { Observable, map, of } from 'rxjs';
import { environment } from '../../environments/environment';

// loads the image with the jwt header and turns it into an object url for <img>
@Pipe({
  name: 'authImage',
  standalone: true
})
export class AuthImagePipe implements PipeTransform {
  private http = inject(HttpClient);
  private cache = new Map<string, string>();

  transform(path: string | null | undefined): Observable<string | null> {
    if (!path) {
      return of(null);
    }
    if (this.cache.has(path)) {
      return of(this.cache.get(path)!);
    }
    // backend path already starts with /api and apiUrl ends with /api, strip one so it's not doubled
    const url = path.startsWith('/api')
      ? `${environment.apiUrl.replace(/\/api$/, '')}${path}`
      : `${environment.apiUrl}${path}`;
    return this.http.get(url, { responseType: 'blob' }).pipe(
      map(blob => {
        const objectUrl = URL.createObjectURL(blob);
        this.cache.set(path, objectUrl);
        return objectUrl;
      })
    );
  }
}
