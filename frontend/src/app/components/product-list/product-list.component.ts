import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

@Component({
  selector: 'app-product-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './product-list.component.html',
  styleUrls: ['./product-list.component.scss']
})
export class ProductListComponent implements OnInit {
  products: any[] = [];
  produtosFiltrados: any[] = [];

  filtros = {
    minPrice: null,
    maxPrice: null,
    search: ''
  };

  constructor(private http: HttpClient, private router: Router) {}

  ngOnInit(): void {
    this.http.get<any[]>('http://localhost:8080/products').subscribe(data => {
      this.products = data;
      this.produtosFiltrados = data;
    });
  }

  aplicarFiltros(): void {
    const { minPrice, maxPrice, search } = this.filtros;

    this.produtosFiltrados = this.products.filter(produto => {
      const nomeMatch = produto.name
        .toLowerCase()
        .includes(search.toLowerCase());

      const precoMatch =
        (minPrice == null || produto.price >= minPrice) &&
        (maxPrice == null || produto.price <= maxPrice);

      return nomeMatch && precoMatch;
    });
  }

  criarProduto(): void {
    this.router.navigate(['/products/new']);
  }
}
