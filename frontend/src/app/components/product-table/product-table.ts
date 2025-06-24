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
  aplicarDesconto(produto: Product): void {
  const entrada = prompt(`Digite o percentual de desconto para "${produto.name}":`);
  const percent = Number(entrada);

  if (isNaN(percent) || percent <= 0 || percent > 90) {
    alert('Digite um número válido entre 1 e 90.');
    return;
  }

  this.productService.aplicarDesconto(produto.id!, percent).subscribe({
    next: () => {
      alert('Desconto aplicado!');
      this.filtrar(); 
    },
    error: (err) => {
      console.error('Erro ao aplicar desconto:', err);
      alert('Erro ao aplicar desconto.');
    }
  });
}
excluirProduto(produto: Product): void {
  if (!confirm(`Tem certeza que deseja excluir "${produto.name}"?`)) return;

  this.productService.excluirProduto(produto.id!).subscribe({
    next: () => {
      alert('Produto inativado com sucesso!');
      this.filtrar(); 
    },
    error: (err) => {
      console.error('Erro ao excluir produto:', err);
      alert('Erro ao excluir produto.');
    }
  });
}
}
