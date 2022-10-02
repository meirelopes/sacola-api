package me.dio.sacola.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import me.dio.sacola.enumeration.FormaPagamento;
import me.dio.sacola.model.Item;
import me.dio.sacola.model.Restaurante;
import me.dio.sacola.model.Sacola;
import me.dio.sacola.repository.ProdutoRepository;
import me.dio.sacola.repository.SacolaRepository;
import me.dio.sacola.resource.ItemDto;
import me.dio.sacola.service.SacolaService;

@RequiredArgsConstructor
@Service
public class SacolaServiceImpl implements SacolaService {

	@Autowired
	private final SacolaRepository sacolaRepository;
	
	@Autowired
	private final ProdutoRepository produtoRepository;
	
	
	@Override
	public Sacola verSacola(Long id) {
		return sacolaRepository.findById(id).orElseThrow (() -> {
			throw new RuntimeException("Essa sacola não existe!");
		});
	}

	@Override
	public Sacola fecharSacola(Long id, int numeroformaPagamento) {
		Sacola sacola = verSacola(id);
		if(sacola.getItens().isEmpty()) {
			throw new RuntimeException("Inclua ítens na sacola!");
		} 
		
		FormaPagamento formaPagamento =  numeroformaPagamento == 0 ? FormaPagamento.DINHEIRO : FormaPagamento.DINHEIRO;
		
		sacola.setFormaPagamento(formaPagamento);
		sacola.setFechada(true);
		sacolaRepository.save(sacola);
		return sacola;
	}

	@Override
	public Item incluirItemNaSacola(ItemDto itemDto) {
		Sacola sacola = verSacola(itemDto.getSacolaId());
		
		
		
		if(sacola.isFechada()) {
			throw new RuntimeException("Esta sacola está fechada!");

		}
		Item itemParaSerInserido =  Item.builder()
			.quantidade(itemDto.getQuantidade())
			.sacola(sacola)
			.produto(produtoRepository.findById(itemDto.getProdutoId()).orElseThrow(()
					-> {
						throw new RuntimeException("Esse produto não existe!");

					}))
			.build();
		
		List<Item> itensDaSacola = sacola.getItens();
		if(itensDaSacola.isEmpty()) {
			itensDaSacola.add(itemParaSerInserido);
		} else {
			Restaurante restauranteAtual = itensDaSacola.get(0).getProduto().getRestaurante();
			Restaurante restauranteDoItemParaAdicionar =  itemParaSerInserido.getProduto().getRestaurante();
			if(restauranteAtual.equals(restauranteDoItemParaAdicionar)) {
				itensDaSacola.add(itemParaSerInserido);
			} else {
				throw new RuntimeException("Não é possível adicionar produtos de restaurantes diferentes. Feche a sacola ou esvazie!");

			}
		}
		
		List<Double> valorDosItens = new ArrayList<>();
		for (Item itemDaSacola : itensDaSacola) {
			double valorTotalItem = 
					itemDaSacola.getProduto().getValorUnitario() * itemDaSacola.getQuantidade();
			valorDosItens.add(valorTotalItem);
		}
		
		double valorTotalSacola = valorDosItens.stream()
				.mapToDouble(valorTotalDeCadaItem -> valorTotalDeCadaItem).sum();
		
		sacola.setValorTotal(valorTotalSacola);
		
		sacolaRepository.save(sacola);

		return itemParaSerInserido;
	}

}
