import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import { Product } from '../models/product.model';
import { environment } from '../environments/environment';

@Injectable({ providedIn: 'root' })
export class ProductService {
  private readonly baseUrl = `${environment.apiUrl}/products`;

  constructor(private http: HttpClient) { }

  /** Busca todos os produtos (com filtros opcionais) */
  listarProdutos(filtros?: Record<string, any>): Observable<Product[]> {
    if (filtros) {
      const params = new HttpParams({ fromObject: filtros });
      return this.http.get<Product[]>(this.baseUrl, { params });
    }
    return this.http.get<Product[]>(this.baseUrl);
  }

  /** Busca um produto por ID */
  getProdutoPorId(id: number): Observable<Product> {
    return this.http.get<Product>(`${this.baseUrl}/${id}`);
  }

  /** Cria um novo produto */
  salvarProduto(produto: Product): Observable<Product> {
    return this.http.post<Product>(this.baseUrl, produto);
  }

  /** Atualiza completamente um produto existente */
  atualizarProduto(produto: Product): Observable<Product> {
    return this.http.put<Product>(
      `${this.baseUrl}/${produto.id}`,
      produto
    );
  }

  /** Atualização parcial de campo(s) via JSON-Patch */
  patchProduto(id: number, patch: any[]): Observable<Product> {
    return this.http.patch<Product>(`${this.baseUrl}/${id}`, patch);
  }

  /** Remove (soft delete) um produto */
  excluirProduto(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }

  /** Restaura um produto previamente inativado */
  restaurarProduto(id: number): Observable<Product> {
    return this.http.post<Product>(`${this.baseUrl}/${id}/restore`, null);
  }

  /** Aplica desconto percentual */
  aplicarDescontoPercentual(id: number, percent: number): Observable<Product> {
    return this.http.post<Product>(
      `${this.baseUrl}/${id}/discount/percent`,
      { percent }
    );
  }

  /** Aplica desconto por cupom */
  aplicarDescontoPorCupom(id: number, couponCode: string): Observable<Product> {
    return this.http.post<Product>(
      `${this.baseUrl}/${id}/discount/coupon`,
      null,
      { params: new HttpParams().set('coupon', couponCode) }
    );
  }

  /** Atualiza só o estoque (exemplo de JSON-Patch inline) */
  atualizarEstoque(id: number, novoEstoque: number): Observable<Product> {
    const patch = [{ op: 'replace', path: '/stock', value: novoEstoque }];
    return this.patchProduto(id, patch);
  }
}
