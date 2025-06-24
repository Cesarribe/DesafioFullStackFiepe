import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClientModule, HttpParams } from '@angular/common/http';
import { Product } from '../../models/product.model';
import { ProductService } from '../../services/product.service';

@Component({
  selector: 'app-product-table',
  standalone: true,
  imports: [CommonModule, FormsModule, HttpClientModule],
  templateUrl: './product-table.html',
  styleUrls: ['./product-table.scss']
})
export class ProductTableComponent implements OnInit {
  produtos: Product[] = [];
  filtros: any = {
    name: '',
    category: '',
    estoqueMin: ''
  };

  constructor(private productService: ProductService) {}

  ngOnInit(): void {
    this.filtrar();
  }

  filtrar(): void {
    const params: any = {};
    if (this.filtros.name) params.name = this.filtros.name;
    if (this.filtros.category) params.category = this.filtros.category;
    if (this.filtros.estoqueMin) params.estoqueMin = this.filtros.estoqueMin;

    this.productService.listarComFiltros(params).subscribe({
      next: (res) => (this.produtos = res),
      error: (err) => console.error('Erro ao aplicar filtros:', err)
    });
  }
}
