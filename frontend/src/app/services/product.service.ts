import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Product } from '../models/product.model';
import { Observable } from 'rxjs';
import { HttpParams } from '@angular/common/http';


@Injectable({
  providedIn: 'root'
})
export class ProductService {
private apiUrl = 'http://localhost:8080/products';

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
aplicarDesconto(id: number, percent: number): Observable<Product> {
  return this.http.post<Product>(`${this.apiUrl}/${id}/discount/percent`, { percent });
}

}

