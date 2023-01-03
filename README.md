# GovIO - Batch di spedizione messaggi

Engine di spedizione dei messaggi al BackendIO.

# Formato dei CSV

I CSV prevedono una serie di parametri standard seguiti da quelli opzionali definiti dal template associato al servizio. Sono previste due serie di parametri standard nel caso in cui il template preveda o meno un avviso di pagamento associato.

## Base

| # | Nome | Obb. | Descrizione | Esempio |
| --- | --- | --- | --- | --- |
| 1 | taxcode | si | Codice fiscale destinatario | RSSMRO00A00A000A |
| 2 | expedition_date | si | Data di spedizione della notifica | 2027-12-03T10:15:30 |
| 3 | due_date | no | Data scadenza | 2027-12-03T10:15:30 |

## Con avviso di pagamento

| # | Nome | Descrizione | Esempio |
| --- | --- | --- | --- |
| 1 | taxcode | si | Codice fiscale destinatario | RSSMRO00A00A000A |
| 2 | expedition_date | si | Data di spedizione della notifica | 2027-12-03T10:15:30 |
| 3 | due_date | no | Data di scadenza dell'avviso | 2027-12-03T10:15:30 |
| 4 | notice_number | si | Numero dell'avviso di pagamento | 200000000000000000 |
| 5 | amount | si | Importo in centesimi | 1000000000000 |
| 6 | invalid_afted_due_date | no | Se true, avviso non valido dopo la data di scadenza | false |
| 7 | payee_taxcode | no | Codice fiscale dell'ente creditore | 12345678901 |



# Placeholders

All'interno del messaggio o dell'oggetto è possibile inserire dei placeholder che GovIO sostituisce con i valori dei parametri forniti in precedenza all'atto della spedizione. I placeholder ammessi dipendono dal tipo del parametro:

## Placeholder di date

| Nome | Descrizione | Esempio |
|  --- | --- | --- |
| `${nome_parametro}` | Standard | 03/12/2027 10:15 |
| `${nome_parametro}.date` | Data senza orario | 03/12/2027 |
| `${nome_parametro}.date.verbose` | Data con giorno senza orario | Mercoledì 03/12/2027 |
| `${nome_parametro}.verbose` | Data con giorno e orario | Mercoledì 03/12/2027 alle ore 10:15 |
| `${nome_parametro}.time` | Orario senza data | 10:15  |

## Placeholder di stringhe

| Nome | Descrizione | Esempio |
|  --- | --- | --- |
| `${nome_parametro}` | Standard | Lorem Ipsum |
| `${nome_parametro}.lower` | Convertito in lowercase | lorem ipsum |
| `${nome_parametro}.upper` | Convertito in uppercase | LOREM IPSUM |

## Placeholder di numeri

| Nome | Descrizione | Esempio |
|  --- | --- | --- |
| `${nome_parametro}` | Standard | 123456 |
| `${nome_parametro}.currency` | Serializzato come importo | 1.234,56 |

## Placeholder di boolean

| Nome | Descrizione | Esempio |
|  --- | --- | --- |
| `${nome_parametro}` | Standard | `True` o `False` |
