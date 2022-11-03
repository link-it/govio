package it.govio.msgsender.writer;
	import java.util.List;

import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import it.govio.msgsender.entity.GovioMessageEntity;
import it.govio.msgsender.service.MsgsenderGovioService;

	@Component
	public class GetProfileWriter implements ItemWriter<GovioMessageEntity>{
		

//		private Logger logger = LoggerFactory.getLogger(RiversamentoPagoPaWriter.class);
		
		@Autowired
		private MsgsenderGovioService msgsenderGovioService;


		@Override
		public void write(List<? extends GovioMessageEntity> items) throws Exception {
			if(!CollectionUtils.isEmpty(items)) {
				doWrite(items);
			}
		}

		protected void doWrite(List<? extends GovioMessageEntity> items) throws Exception {
			for (GovioMessageEntity msgGovio : items) {
				this.msgsenderGovioService.saveMessageGovio(msgGovio);
			}
		}
	}
