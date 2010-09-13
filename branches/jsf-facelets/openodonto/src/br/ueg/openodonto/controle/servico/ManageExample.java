package br.ueg.openodonto.controle.servico;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import br.ueg.openodonto.persistencia.orm.OrmFormat;
import br.ueg.openodonto.persistencia.orm.OrmResolver;
import br.ueg.openodonto.persistencia.orm.OrmTranslator;
import br.ueg.openodonto.servico.busca.InputField;
import br.ueg.openodonto.servico.busca.SearchFilter;
import br.ueg.openodonto.validator.Validator;

public class ManageExample<T> implements Serializable{
	private static final long serialVersionUID = -9067163321882594609L;
	private Class<T> classe;
	
	public ManageExample(Class<T> classe) {
		this.classe = classe;
	}
	
	private T factoryExample(){
		try {
			return classe.newInstance();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private SearchFilter findSearchFilter(String name,List<SearchFilter> filters){
		for(Iterator<SearchFilter> iterator = filters.iterator();iterator.hasNext();){
			SearchFilter filter;
			if((filter = iterator.next()).getName().equals(name)){
				return filter;
			}
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	private InputField<String> getCommonInput(SearchFilter filter){
		return (InputField<String>) filter.getField().getInputFields().get(0);
	}
	
	private boolean checkValidators(List<Validator> validators){
		boolean valid = true;
		for(Iterator<Validator> iterator = validators.iterator();iterator.hasNext();valid = valid && iterator.next().isValid());
		return valid;
	}
	
	private boolean checkInvalidPermiteds(List<Class<?>> invalidPermiteds,Validator validator){
		Iterator<Class<?>> iterator  = invalidPermiteds.iterator();
		boolean valid = true;
		while(iterator.hasNext()){
			Class<?> permited = iterator.next();
			valid = valid && !permited.isAssignableFrom(validator.getSource().getClass());
		}
		return valid;
	}
	
	private void notifyValidation(SearchFilter filter,InputField<?> inputField,List<Class<?>> invalidPermiteds){
		for(Iterator<Validator> iterator = inputField.getValidators().iterator();iterator.hasNext();){
			Validator validator = iterator.next();
			if(!validator.isValid() && checkInvalidPermiteds(invalidPermiteds,validator)){
				filter.displayValidationMessage("* " + filter.getLabel() + " = '" + inputField.getValue() + "' : " + validator.getErrorMessage());
			}
		}
	}
	
  	public T processExampleRequest(ExampleRequest<T> req){
		Map<String, Object> values = new HashMap<String, Object>();
		OrmTranslator translator = new OrmTranslator(OrmResolver.getAllFields(new ArrayList<Field>(), classe, true));
		Iterator<ExampleRequest<T>.TypedFilter> iterator = req.getFilterRelation().iterator();
		while(iterator.hasNext()){
			ExampleRequest<T>.TypedFilter typedFilter  = iterator.next();
			SearchFilter filter = findSearchFilter(typedFilter.getFilterName(),req.getSearchable().getFilters());
			InputField<String> inputField = getCommonInput(filter);
			boolean valid = checkValidators(inputField.getValidators());
			if(valid){
				String field = typedFilter.getBeanPath();
				String column = translator.getColumn(field);
				values.put(column, inputField.getValue());
			}else{
				notifyValidation(filter,inputField,req.getInvalidPermiteds());
			}
		}
		T bean = factoryExample();
		OrmFormat format = new OrmFormat(bean);
		format.parse(values);
		return bean;
	}
	
}
