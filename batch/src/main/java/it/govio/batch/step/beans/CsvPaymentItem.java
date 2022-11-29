package it.govio.batch.step.beans;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter	
@AllArgsConstructor
@NoArgsConstructor
public class CsvPaymentItem extends CsvItem {
	
	String noticeNumber;
	long amount;
	String payeeTaxcode;
	LocalDateTime dueDate;
	boolean invalidAfterDueDate;

}
