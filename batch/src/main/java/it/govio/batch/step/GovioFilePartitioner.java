package it.govio.batch.step;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;

import it.govio.batch.entity.GovioFileEntity;

public class GovioFilePartitioner implements Partitioner {

	List<GovioFileEntity> govioFileEntities;
	private Logger logger = LoggerFactory.getLogger(GovioFilePartitioner.class);
	
	@Override
	public Map<String, ExecutionContext> partition(int gridSize) {
		Map<String, ExecutionContext> result = new HashMap<> (gridSize);
		for (GovioFileEntity file : govioFileEntities){
			// Devo recuperare la lista dei placeholder previsti
			ExecutionContext ex = new ExecutionContext();
			ex.putString("location", file.getLocation());
			ex.putLong("id", file.getId());
			if (file.getGovioServiceInstance().getGovioTemplate() == null) 
				ex.put("template", file.getGovioServiceInstance().getIdGovioService().getGovioTemplate());
			ex.put("template", file.getGovioServiceInstance().getGovioTemplate());
			ex.putLong("serviceInstance", file.getGovioServiceInstance().getId());
			result.put("F"+file.getId(), ex);
			logger.debug("ExecutionContext {} aggiunto [id:{}, location:{}, template:{}, serviceInstance: {}]", 
					"F"+file.getId(), 
					file.getId(), 
					file.getLocation(), 
					file.getGovioServiceInstance().getGovioTemplate().getId(),
					file.getGovioServiceInstance().getId());
		}
		return result;
	}

	public void setGovioFileEntities(List<GovioFileEntity> govioFileEntities) {
		this.govioFileEntities = govioFileEntities;
	}
}
