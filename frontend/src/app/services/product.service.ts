import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Product } from '../models/product.model';
import { Observable } from 'rxjs';
import { HttpParams } from '@angular/common/http';


@Injectable({
  providedIn: 'root'
})
export class ProductService {
  private apiUrl = 'http://localhost:3000/products'; // ou o endpoint da sua API

  constructor(private http: HttpClient) {}

  listarProdutos(): Observable<Product[]> {
    return this.http.get<Product[]>(this.apiUrl);
  }

  salvarProduto(produto: Product): Observable<Product> {
    return this.http.post<Product>(this.apiUrl, produto);
  }

  atualizarProduto(produto: Product): Observable<Product> {
    return this.http.put<Product>(`${this.apiUrl}/${produto.id}`, produto);
  }

  excluirProduto(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
  
  listarComFiltros(filtros: any): Observable<Product[]> {
  const params = new HttpParams({ fromObject: filtros });
  return this.http.get<Product[]>(this.apiUrl, { params });
}

}

