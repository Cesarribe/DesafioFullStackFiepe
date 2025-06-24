import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Product } from '../models/product.model';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ProductService {
  private apiUrl = 'http://localhost:3000/products'; 

  constructor(private http: HttpClient) {}

  salvarProduto(produto: Product): Observable<Product> {
    return this.http.post<Product>(this.apiUrl, produto);
  }

  listarProdutos(): Observable<Product[]> {
    return this.http.get<Product[]>(this.apiUrl);
  }
}
