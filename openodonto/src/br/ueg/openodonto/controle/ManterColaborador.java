package br.ueg.openodonto.controle;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import br.ueg.openodonto.controle.busca.ResultFacadeBean;
import br.ueg.openodonto.controle.busca.SearchBase;
import br.ueg.openodonto.controle.busca.SearchableColaborador;
import br.ueg.openodonto.controle.servico.ManageTelefone;
import br.ueg.openodonto.controle.servico.ValidationRequest;
import br.ueg.openodonto.dominio.Colaborador;
import br.ueg.openodonto.dominio.ColaboradorProduto;
import br.ueg.openodonto.dominio.Produto;
import br.ueg.openodonto.dominio.constante.CategoriaProduto;
import br.ueg.openodonto.dominio.constante.TipoPessoa;
import br.ueg.openodonto.persistencia.dao.DaoColaboradorProduto;
import br.ueg.openodonto.persistencia.dao.DaoFactory;
import br.ueg.openodonto.servico.busca.MessageDisplayer;
import br.ueg.openodonto.servico.busca.ResultFacade;
import br.ueg.openodonto.servico.busca.Search;
import br.ueg.openodonto.validator.ValidatorFactory;

public class ManterColaborador extends ManageBeanGeral<Colaborador> {

	private static final long serialVersionUID = 7597358634869495788L;
	
	private ManageTelefone                manageTelefone;
	private static Map<String, String>    params;
	private Search<Colaborador>           search;
	private MessageDisplayer              displayer;	
	private CategoriaProduto              categoria;
	private TipoPessoa                    tipoPessoa;
	
	static{
		params = new HashMap<String, String>();
		params.put("managebeanName", "manterColaborador");
		params.put("formularioSaida", "formColaborador");
		params.put("formModalSearch", "formSearch");
		params.put("nameModalSearch", "painelBusca");
		params.put("saidaPadrao", "formColaborador:output");
		params.put("saidaContato", "messageTelefone");
	}
	
	public ManterColaborador() {
		super(Colaborador.class);
	}

	@Override
	protected void initExtra() {
		this.displayer = new ViewDisplayer("searchDefaultOutput");
		this.manageTelefone = new ManageTelefone(getColaborador().getTelefone(), this);
		this.search = new SearchBase<Colaborador>(new SearchableColaborador(),
				"Buscar Colaborador",
				"painelBusca",
				this.displayer);
		this.search.addSearchListener(new SearchColaboradorHandler());
		this.search.addSearchListener(new SearchSelectedHandler());
		this.tipoPessoa = TipoPessoa.PESSOA_FISICA;
		makeView(params);
	}
	
	@Override
	public void acaoSalvar() {
		clearUnsedDocument();
		super.acaoSalvar();
	}
	
	@Override
	public void acaoSalvarExtra(){
		getColaborador().setCategoria(getCategoria());
	}
	
	private void clearUnsedDocument(){
		switch (tipoPessoa) {
		case PESSOA_FISICA:
			getColaborador().setCnpj(null);
			break;
		case PESSOA_JURIDICA:	
			getColaborador().setCpf(null);
			break;
		}
	}
	
	@Override
	protected List<String> getCamposFormatados() {
		List<String> formatados = new ArrayList<String>();
		formatados.add("nome");
		formatados.add("cidade");
		formatados.add("endereco");
		formatados.add("cpf");
		formatados.add("cnpj");
		return formatados;
	}

	@Override
	protected List<ValidationRequest> getCamposObrigatorios() {
		List<ValidationRequest> obrigatorios = new ArrayList<ValidationRequest>();
		obrigatorios.add(new ValidationRequest("nome", ValidatorFactory.newSrtEmpty(), "formColaborador:entradaNome"));
		if(getTipoPessoa().isPf()){
			obrigatorios.add(new ValidationRequest("cpf",ValidatorFactory.newCpf(),"formColaborador:entradaCpf"));
		}else if(getTipoPessoa().isPj()){
			obrigatorios.add(new ValidationRequest("cnpj",ValidatorFactory.newCnpj(),"formColaborador:entradaCnpj"));
		}
		return obrigatorios;
	}
	
	@Override
	protected void carregarExtra() {
		manageTelefone.setTelefones(getColaborador().getTelefone());
	}	
	
	public Colaborador getColaborador(){
		return getBackBean();
	}
	
	public CategoriaProduto getPrestadorMode(){
		this.categoria = CategoriaProduto.SERVICO;
		return getCategoria();
	}
	
	public CategoriaProduto getFornecedorMode(){
		this.categoria = CategoriaProduto.PRODUTO;
		return getCategoria();
	}	
	
	public Search<Colaborador> getSearch() {
		return search;
	}

	public TipoPessoa getTipoPessoa() {
		return tipoPessoa;
	}

	public void setTipoPessoa(TipoPessoa tipoPessoa) {
		this.tipoPessoa = tipoPessoa;
	}

	public CategoriaProduto getCategoria() {
		return categoria;
	}
	
	public void setColaborador(Colaborador colaborador){
		setBackBean(colaborador);
	}	
	
	public ManageTelefone getManageTelefone() {
		return manageTelefone;
	}

	public void setManageTelefone(ManageTelefone manageTelefone) {
		this.manageTelefone = manageTelefone;
	}

	protected List<ResultFacade> wrapResult(List<Map<String, Object>> result) {
		List<ResultFacade> resultWrap = new ArrayList<ResultFacade>(result.size());
		Iterator<Map<String, Object>> iterator = result.iterator();
		while(iterator.hasNext()){
			Map<String,Object> value = iterator.next();
			Object cpf = value.get("cpf");
			Object cnpj = value.get("cnpj");
			String documento = (cpf == null ? (cnpj != null ? cnpj : "") : cpf).toString();
			value.put("documento", documento);
			resultWrap.add(new ResultFacadeBean(value));
		}
		return resultWrap;
	}
	
	protected class SearchColaboradorHandler extends SearchBeanHandler<Colaborador> implements Serializable{
		private static final long serialVersionUID = 5660539094298081485L;
		private String[] showColumns = {"codigo","nome","email","cpf","cnpj"};
		@Override
		public String[] getShowColumns() {
			return showColumns;
		}
		@Override
		protected List<ResultFacade> wrapResult(List<Map<String, Object>> result) {
			return ManterColaborador.this.wrapResult(result);
		}
		public List<Map<String,Object>> evaluteResult(Search<Colaborador> search) throws SQLException{
			SearchableColaborador searchable = (SearchableColaborador)search.getSearchable();
			searchable.setCategoria(getCategoria());
			Colaborador colaborador = searchable.buildExample();
			Produto produto = searchable.buildExampleProduto();
			DaoColaboradorProduto dao = (DaoColaboradorProduto)DaoFactory.getInstance().getDao(ColaboradorProduto.class);
			List<Map<String,Object>> result;
			if(produto == null){
				result = getDao().getSqlExecutor().executarUntypedQuery(getQuery(colaborador));
			}else{
				result = dao.getUntypeColaboradores(produto,colaborador);
			}
			return result;			
		}
		
	}

}