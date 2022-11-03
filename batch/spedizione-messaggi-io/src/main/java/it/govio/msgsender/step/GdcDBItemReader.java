package it.govio.msgsender.step;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import it.govio.msgsender.entity.GovioMessageEntity;
import it.govio.msgsender.entity.GovioMessageEntity.Status;
import it.govio.msgsender.repository.GovioMessagesRepository;

@Component
public class GdcDBItemReader extends RepositoryItemReader<GovioMessageEntity>{

	@Autowired
	private GovioMessagesRepository riversamentoPagoPaRepository;
	
	@PostConstruct
	public void initDBReader() {
		this.setRepository(this.riversamentoPagoPaRepository);
		this.setMethodName("findAllByStatoIn");
		List<Status> stati = Arrays.asList(Status.SCHEDULED);
		this.setArguments(Arrays.asList(stati));
		this.setPageSize(1);
		final HashMap<String, Sort.Direction> sorts = new HashMap<>();
		sorts.put("id", Sort.Direction.ASC);
		this.setSort(sorts);
	}
}