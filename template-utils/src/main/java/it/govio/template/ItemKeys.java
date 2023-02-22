package it.govio.template;

public enum ItemKeys {
	TAXCODE("taxcode"), 
	EXPEDITIONDATE("expedition_date"), 
	DUEDATE("due_date"), 
	NOTICENUMBER("notice_number"), 
	AMOUNT("amount"), 
	INVALIDAFTERDUEDATE("invalid_after_due_date"), 
	PAYEE("payee");

	private String string;

	ItemKeys(String string) {
		this.string = string;
	}  

	@Override
	public String toString() {
		return string;
	}
}
