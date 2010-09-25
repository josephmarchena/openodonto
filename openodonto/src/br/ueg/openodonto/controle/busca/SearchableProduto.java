package br.ueg.openodonto.controle.busca;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import br.ueg.openodonto.controle.servico.ExampleRequest;
import br.ueg.openodonto.controle.servico.ManageListagem;
import br.ueg.openodonto.dominio.Colaborador;
import br.ueg.openodonto.dominio.Produto;
import br.ueg.openodonto.dominio.constante.CategoriaProduto;
import br.ueg.openodonto.persistencia.dao.sql.SqlWhereOperatorType;
import br.ueg.openodonto.persistencia.orm.OrmResolver;
import br.ueg.openodonto.persistencia.orm.OrmTranslator;
import br.ueg.openodonto.servico.busca.FieldFacade;
import br.ueg.openodonto.validator.EmptyValidator;
import br.ueg.openodonto.validator.NullValidator;
import br.ueg.openodonto.validator.Validator;
import br.ueg.openodonto.validator.ValidatorFactory;

public class SearchableProduto extends AbstractSearchable<Produto>{

	private static final long serialVersionUID = 844846365772534533L;

	public SearchableProduto(){
		super(Produto.class);
	}
	
	public void buildFacade(){
		super.buildFacade();
		OrmTranslator translator = new OrmTranslator(OrmResolver.getAllFields(new ArrayList<Field>(), Produto.class, true));
		getFacade().add(new FieldFacade("C�digo",translator.getColumn("codigo")));
		getFacade().add(new FieldFacade("Nome",translator.getColumn("nome")));
		getFacade().add(new FieldFacade("Categoria","categoriaDesc"));
		getFacade().add(new FieldFacade("Descri��o","shortDescription"));
	}
	
	public void buildFilter(){
		super.buildFilter();
		buildNameFilter();
		buildCategoriaFilter();
		buildDescricaoFilter();
		buildColaboradorFilter();
	}
	
	private void buildDescricaoFilter() {
		Validator validator = ValidatorFactory.newStrRangeLen(300,3, true);
		getFiltersMap().put("descricaoFilter", buildBasicFilter("descricaoFilter","Descri��o",validator));
	}

	private void buildCategoriaFilter() {
		Validator validator = ValidatorFactory.newNull();
		List<CategoriaProduto> domain = ManageListagem.getLista(CategoriaProduto.class).getDominio(); 
		getFiltersMap().put("categoriaFilter", buildBasicFilter("categoriaFilter","Categoria",domain,validator));
	}

	private void buildNameFilter() {
		Validator validator = ValidatorFactory.newStrRangeLen(150,3, true);
		getFiltersMap().put("nomeFilter", buildBasicFilter("nomeFilter","Nome",validator));
	}

	private void buildColaboradorFilter() {
		Validator validator = ValidatorFactory.newStrRangeLen(100,3, true);
		getFiltersMap().put("colaboradorFilter", buildBasicFilter("colaboradorFilter","Colaborador",validator));
	}
	
	public Colaborador buildExampleColaborador(){
		Object value = getFiltersMap().get("colaboradorFilter").getField().getInputFields().get(0).getValue();
		Colaborador colaborador = null;
		if(value != null && !value.toString().trim().isEmpty()){
			colaborador = new Colaborador();
		    colaborador.setNome("%"+value.toString()+"%");
		}
	    return colaborador;	
	}
	
	public Produto buildExample(){
		ExampleRequest<Produto> request  = new ExampleRequest<Produto>(this);		
		request.getFilterRelation().add(request.new TypedFilter("nomeFilter", "nome",SqlWhereOperatorType.LIKE));
		request.getFilterRelation().add(request.new TypedFilter("categoriaFilter","categoria",SqlWhereOperatorType.EQUAL));
		request.getFilterRelation().add(request.new TypedFilter("descricaoFilter","descricao",SqlWhereOperatorType.LIKE));
		request.getInvalidPermiteds().add(NullValidator.class);
		request.getInvalidPermiteds().add(EmptyValidator.class);
		Produto target = getManageExample().processExampleRequest(request);
		return target;
	}

}