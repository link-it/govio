package it.govhub.govio.api.repository;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;

import it.govhub.govio.api.entity.GovioFileEntity_;
import it.govhub.govio.api.entity.GovioFileMessageEntity;
import it.govhub.govio.api.entity.GovioFileMessageEntity_;

public class FileMessageFilters {

       public static Specification<GovioFileMessageEntity> empty() {
	               return (Root<GovioFileMessageEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> null; 
	       }
	       
	       public static Specification<GovioFileMessageEntity> fromLineNumber(Long lineNumberFrom) {
	               return (Root<GovioFileMessageEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> 
	                       cb.greaterThanOrEqualTo(root.get(GovioFileMessageEntity_.lineNumber), lineNumberFrom);
	       }
	       
	       
	       public static Specification<GovioFileMessageEntity> ofFile(Long govioFileId) {
	               return (Root<GovioFileMessageEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> 
	                       cb.equal(root.get(GovioFileMessageEntity_.govioFile).get(GovioFileEntity_.id), govioFileId);
	       }
	       
	       /**
	        * Restituisce i fileMessages in stato logico 'ACQUIRED' ovvero con un messaggio collegato.
	        */
	       public static Specification<GovioFileMessageEntity> acquired() {
	               return (Root<GovioFileMessageEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) ->
	                       cb.isNotNull(   root.get(GovioFileMessageEntity_.govioMessage));
	       }
	       
	       
	       /**
	        * Restituisce i fileMessages in stato logico 'ERROR' ovvero senzaun messaggio collegato.
	        */
	       public static Specification<GovioFileMessageEntity> error() {
	               return (Root<GovioFileMessageEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) ->
	                       cb.isNull(      root.get(GovioFileMessageEntity_.govioMessage));
	       }
	       
	       
	       public static Sort sortByLineNumber() {
	               return Sort.by(Direction.ASC, GovioFileMessageEntity_.LINE_NUMBER);
	       }

}
