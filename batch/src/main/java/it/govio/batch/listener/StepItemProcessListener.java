package it.govio.batch.listener;

import java.util.List;

import org.springframework.batch.core.ItemProcessListener;
import org.springframework.web.client.HttpClientErrorException;

import it.govio.batch.entity.GovioMessageEntity;

public class StepItemProcessListener implements ItemProcessListener<GovioMessageEntity, GovioMessageEntity> {
	@Override
	public void beforeProcess(GovioMessageEntity item) {
		System.out.println("ItemProcessListener - beforeProcess");
	}
	@Override
	public void afterProcess(GovioMessageEntity item, GovioMessageEntity result) {
		System.out.println("ItemProcessListener - afterProcess");
	}
		
	@Override
	public void onProcessError(GovioMessageEntity item, Exception e) {
		try {
			System.out.println("ItemProcessListener - onError");
			if (e.getClass() == HttpClientErrorException.class) {
				HttpClientErrorException e2 = (HttpClientErrorException) e;
				if (e2.getRawStatusCode() == 429) {
					List<String> values = e2.getResponseHeaders().getValuesAsList("Retry-After");
					String value = values.get(0);
					Thread.sleep(1000);
				}
				
				System.out.println("ItemProcessListener - onError");
			}
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
}