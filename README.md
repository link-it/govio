
# GovIO - Batch di spedizione messaggi

Engine di spedizione dei messaggi al BackendIO.

# Formato dei CSV

## Senza avviso

| # | Nome | Descrizione | Esempio |
| --- | --- | --- | --- |
| 1 | taxcode | Codice fiscale destinatario | RSSMRO00A00A000A |
| 2 | expedition_date | Data di notifica | 2027-12-03T10:15:30 |
| 3 | due_date | Data scadenza | 2027-12-03T10:15:30 |
| 4 | appointment | Data dell'appuntamento | 2100-12-31 |
| 5 | at | Luogo dell'appuntamento | Ufficio numero 1 |

## Con avviso
| # | Nome | Descrizione | Esempio |
| --- | --- | --- | --- |
| 1 | taxcode | Codice fiscale destinatario | RSSMRO00A00A000A |
| 2 | expedition_date | Data di notifica | 2027-12-03T10:15:30 |
| 3 | due_date | Data scadenza | 2027-12-03T10:15:30 |
| 4 | notice_number | Creditore | 200000000000000000 |
| 5 | amount | Importo in centesimi | 1000000000000 |
| 6 | invalid_afted_due_date | Se true, avviso non valido dopo la data di scadenza | false |
| 7 | payee_taxcode | Creditore | 12345678901 |
| 8 | appointment | Data dell'appuntamento | 2100-12-31 |
| 9 | at | Luogo dell'appuntamento | Ufficio numero 1 |

TBD

# Placeholders

| Nome | Descrizione | Esempio |
|  --- | --- | --- |
| taxcode | Codice fiscale destinatario | RSSMRO00A00A000A |
| expedition_date | Data di notifica | 2027-12-03 10:15 |
| expedition_date.date | Data di notifica senza orario | 2027-12-03 |
| expedition_date.date.verbose | Data di notifica senza orario con giorno della settimana | mer 12 12 2007 |
| expedition_date.time | Orario di notifica senza data  | 10:15  |
| expedition_date.verbose | Data di notifica con giorno della settimana e orario | lun 03 12 2007 alle ore 10:15 |
| due_date | Data scadenza | 2027-12-03 10:15:30 |
| duedate.date | Data scadenza senza orario | 12/12/2022 |
| duedate.date.verbose | Orario scadenza senza data | mer 12 12 2007 |
| duedate.time | Orario scadenza senza data | 10:15 |
| duedate.verbose | Data scadenza con giorno della settimana | mer 12 12 2007 alle ore 10:15 |
| notice_number | Creditore | 200000000000000000 |
| amount | Importo in centesimi | 1000000000000 |
| amount.currency | Importo in euro | 10.000.000.000,00 |
| invalid_afted_due_date | Se true, avviso non valido dopo la data di scadenza | false |
| payee_taxcode | Creditore | 12345678901 |
| appointment | Data dell'appuntamento | 03/12/2007 10:15 |
| appointment.verbose | Data dell'appuntamento con giorno della settimana | mer 31 12 2022 |
| at | Luogo dell'appuntamento | Ufficio numero 1  |
