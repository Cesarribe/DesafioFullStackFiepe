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
  produtosPaginados: any[] = [];

  filtros = {
    minPrice: null,
    maxPrice: null,
    search: '',
    tipo: 'todos' // 'todos' | 'desconto' | 'inativos'
  };

  paginaAtual = 1;
  itensPorPagina = 10;

  constructor(private http: HttpClient, private router: Router) {}

  ngOnInit(): void {
    this.http.get<any[]>('http://localhost:8080/products').subscribe(data => {
      this.products = data;
      this.aplicarFiltros(); // já aplica filtros ao iniciar
    });
  }

  aplicarFiltros(): void {
    const { minPrice, maxPrice, search, tipo } = this.filtros;
    const termo = (search || '').toLowerCase();

    this.produtosFiltrados = this.products
      .filter(produto => {
        if (tipo === 'desconto' && (!produto.discount || produto.discount <= 0)) return false;
        if (tipo === 'inativos' && produto.stock > 0) return false;
        return true;
      })
      .filter(produto => {
        return (
          produto.name.toLowerCase().includes(termo) ||
          produto.description?.toLowerCase().includes(termo)
        );
      })
      .filter(produto => {
        const dentroMin = minPrice == null || produto.price >= minPrice;
        const dentroMax = maxPrice == null || produto.price <= maxPrice;
        return dentroMin && dentroMax;
      });

    this.paginaAtual = 1;
    this.atualizarPaginacao();
  }

  atualizarPaginacao(): void {
    const inicio = (this.paginaAtual - 1) * this.itensPorPagina;
    const fim = this.paginaAtual * this.itensPorPagina;
    this.produtosPaginados = this.produtosFiltrados.slice(inicio, fim);
  }

  totalPaginas(): number {
    return Math.ceil(this.produtosFiltrados.length / this.itensPorPagina);
  }

  mudarPagina(delta: number): void {
    const novaPagina = this.paginaAtual + delta;
    if (novaPagina < 1 || novaPagina > this.totalPaginas()) return;

    this.paginaAtual = novaPagina;
    this.atualizarPaginacao();
  }


  criarProduto(): void {
    this.router.navigate(['/products/new']);
  }

   alternarDesconto(produto: any): void {
  if (produto.hasDiscount) {
    // Remover desconto
    this.http.delete(`http://localhost:8080/products/${produto.id}/discount`)
      .subscribe(() => {
        alert('Desconto removido.');
        this.atualizarProdutos();
      }, () => {
        alert('Erro ao remover desconto.');
      });
  } else {
    // Aplicar desconto com entrada
    const valor = prompt('Informe o desconto (%)');
    const percentual = Number(valor);

    if (isNaN(percentual) || percentual <= 0 || percentual >= 100) {
      alert('Percentual inválido. Use um número entre 1 e 99.');
      return;
    }

    this.http.post(`http://localhost:8080/products/${produto.id}/discount/percent?percent=${percentual}`, {})
      .subscribe(() => {
        alert(`Desconto de ${percentual}% aplicado!`);
        this.atualizarProdutos();
      }, () => {
        alert('Erro ao aplicar desconto.');
      });
  }
}

  atualizarProdutos(): void {
    this.http.get<any[]>('http://localhost:8080/products').subscribe(data => {
      this.products = data;
      this.aplicarFiltros(); // reaplica paginação e filtros
    });
  }
}