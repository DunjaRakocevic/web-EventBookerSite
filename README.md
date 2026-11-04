# Specifikacija projekta

## Platforma za oglašavanje i pregled događaja (RAF Event Booker)

# 1. Pregled zadatka

Zadatak je napraviti online platformu koja omogućava:

1. **Sistem za upravljanje događajima (EMS)** – dostupan samo autentifikovanim korisnicima određenih uloga (event creator ili admin).
2. **Javnu platformu za pregled događaja** – dostupnu svim posetiocima, bez potrebe za prijavom, gde se mogu videti svi dostupni događaji i njihovi detalji.

Osnovni cilj je da organizatori (event creatori) mogu da kreiraju nove događaje, uređuju postojeće, brišu ih i prate komentare i reakcije korisnika, dok admin ima dodatne mogućnosti upravljanja korisnicima. Javna platforma je potrebno da omogući posetiocima da pretražuju i pregledaju sve događaje, vrše prijavu (RSVP) ako je dozvoljeno, komentarišu, lajkuju ili dislajkuju događaje i komentare, prate slične događaje po tagovima, i slično.

# 2. Entiteti

U nastavku su opisani osnovni entiteti i njihovi ključni atributi. Polja koja su neophodna (obavezna) ne smeju biti ni `null` niti prazan string. Pored navedenog, dozvoljeno je dodavanje pomoćnih tabela i ostalih polja potrebnih za dalji razvoj rešenja.

## 2.1. Korisnik

- **email** – jedinstvena email adresa (obavezno, jedinstveno).
- **ime i prezime** – obavezna polja.
- **tip korisnika** – može biti „event creator“ ili „admin“.
- **status (aktivan/neaktivan)** – određuje da li korisnik ima pristup EMS-u.
- **lozinka** – čuva se samo kao heš vrednost.

**Napomena:** Admina je potrebno ručno kreirati u bazi podataka na početku (npr. jednim INSERT upitom), kako bi se omogućio inicijalni pristup sistemu.

## 2.2. Kategorija

- **naziv kategorije** – jedinstveno (obavezno).
- **opis** – kratak opis kategorije (obavezno).

Kategorija opisuje širu oblast, tip ili vrstu događaja (npr. koncerti, konferencije, radionice...).

## 2.3. Događaj

- **naslov** – obavezno.
- **opis** – tekstualni opis događaja (obavezno).
- **vreme kreiranja** – datum i vreme kreiranja događaja (obavezno).
- **datum održavanja** – datum i vreme kada se događaj održava (obavezno).
- **lokacija** – mesto gde se događaj održava (obavezno).
- **broj poseta (views)** – broj pregleda događaja na javnoj platformi (podrazumevano 0).
- **autor (korisnik)** – event creator ili admin koji je kreirao događaj.
- **tagovi** – ključne reči koje opisuju događaj (može ih biti više).
- **kategorija** – jedna kategorija kojoj događaj pripada (obavezno).
- **max kapacitet (opcionalno)** – ako je postavljeno, označava maksimalan broj prisutnih koji mogu da se prijave (RSVP), u suprotnom je neograničeno.

## 2.4. Tag

- **naziv** – ključna reč ili fraza koja opisuje događaj (obavezno).
- Događaj može imati više tagova, a jedan tag može biti dodeljen većem broju događaja.

Primeri tagova: rok, metal, rep (za koncerte), slikanje, kuvanje, vajanje (za radionicu), it, startap, netvorking (za konferencije), itd.

## 2.5. Komentar

- **ime autora komentara** – unosi se ručno prilikom ostavljanja komentara (obavezno).
- **tekst komentara** – obavezno.
- **datum kreiranja** – obavezno.
- **pripada događaju** – svaki komentar se vezuje za jedan konkretan događaj.
- **like/dislike count** – broj lajkova i dislajkova (inicijalno 0).

**Napomena:** Nije potrebno da korisnik bude registrovan da bi ostavio komentar – ostavlja samo ime (Dopuniti da registrovani korisnik koristi svoje ime).

## 2.5. RSVP / Prijave (entitet za evidentiranje prijava korisnika na događaj)

- korisnički identifikator (može biti ID ili email).
- ID događaja.
- datum prijave.

Može se ograničiti kapacitet prijava na osnovu max kapaciteta ako je on definisan.

# 3. EMS (Sistem za upravljanje događajima)

## 3.1. Autentifikacija i autorizacija

- Pristup EMS-u zahteva autentifikaciju.
- Postoje 2 tipa korisnika:
  - **event creator** – kreira i uređuje događaje, pregleda i upravlja komentarima na te događaje.
  - **admin** – sve mogućnosti event creatora plus upravljanje korisnicima.
- Stranica za prijavu sadrži polja: email, lozinka i dugme „Prijava“. Nakon unosa proverava se da li postoji aktivan korisnik sa odgovarajućim email-om i heširanom lozinkom.
- Status korisnika mora biti aktivan da bi se pristupilo EMS-u.
- Autentifikacija se može implementirati putem tokena, sesije ili kolačića – ne mora da ima rok trajanja.
- Autorizacija na backendu: ako je korisnik tipa event creator, ne sme imati pristup dodavanju/izmeni korisnika (to je samo za admina), itd.

## 3.2. Funkcionalnosti po tipu korisnika

### 3.2.1. Event creator

Nakon uspešne prijave, event creator vidi navigacioni meni sa:

- Kategorije
- Događaji
- Pretraga događaja

#### (a) Kategorije

Stranica prikazuje tabelu svih postojećih kategorija (naziv, opis).

Tu se nalaze akcije:

- **Dodaj novu kategoriju** (forma sa nazivom i opisom; nije dozvoljeno dupliranje naziva).
- **Izmeni** (forma koja prikazuje trenutne vrednosti i omogućava izmene).
- **Obriši** (nije dozvoljeno brisanje ako postoji barem jedan događaj u toj kategoriji).

#### (b) Događaji

Stranica sadrži tabelu svih događaja, sortiranu po datumu kreiranja (opadajuće), uz paginaciju. Svaki red u tabeli prikazuje:

- Naslov događaja
- Ime autora (ko je kreirao)
- Datum kreiranja
- Akcije: izmena i brisanje

Iznad tabele je dugme Dodaj novi događaj, koje vodi na formu. Forma ima:

- Naslov
- Opis
- Datum i vreme održavanja
- Lokacija
- Kategorija (iz postojeće liste)
- Tagovi (tekstualno polje, unose se proizvoljni tagovi razdvojeni zapetama, povlakama ili nečim drugim)
- (Opciono) Max kapacitet.

Klik na naslov događaja vodi na javnu platformu, gde se taj događaj vidi u novom tabu.

- **Izmena događaja** – forma slična formi za kreiranje, popunjena trenutnim vrednostima.
- **Brisanje** – briše događaj i sve komentare koji su vezani za taj događaj.

#### (c) Pretraga događaja

Polje za unos teksta; sistem pretražuje po naslovu ili po opisu događaja.

Rezultat se prikazuje u tabeli, uz paginaciju, na isti način kao i za „Događaji“.

### 3.2.2. Admin

Pored svih funkcionalnosti koje ima event creator, admin ima mogućnost i da upravlja svim korisnicima.

Navigacioni meni za admina ima dodatnu stavku: **Korisnici**

**Korisnici** – stranica sa tabelom svih korisnika (ime, prezime, email, tip korisnika).

Akcije:

- **Dodaj novog korisnika** (forma sa ime, prezime, email, tip, lozinka i polje za potvrdu lozinke).
- **Izmeni** (prikaz trenutnih vrednosti za ime, prezime, email, tip).
- **Aktiviraj/Deaktiviraj** (samo za event creatore; admin ostaje uvek aktivan). Deaktivirani event creator više ne može da pristupi EMS-u.

U gornjem desnom uglu menija prikazati ime ulogovanog korisnika i dugme „Logout“.

# 4. Javna platforma za pregled događaja

Dostupna svim posetiocima (nije potrebna prijava). Navigacioni meni sadrži:

- Početna (Home page)
- Najposećeniji
- Kategorije (lista linkova)
- Polje za pretragu (po naslovu/tekstu)

## 4.1. Home page

- Prikazuje 10 najnovijih događaja, sortiranih po datumu kreiranja (opadajuće).
- Svaki događaj je predstavljen osnovnim informacijama (naslov, ograničen deo opisa, datum objave, kojoj kategoriji pripada).

## 4.2. Najposećeniji

- Prikazuje 10 događaja sa najvećim brojem pregleda (views) u poslednjih 30 dana.
- Stavka u listi uključuje naslov i kratki opis.

## 4.3. Kategorije

- Klikom na neku kategoriju prikazuju se svi događaji iz te kategorije, paginirano.
- Svaka stavka prikazuje naslov, kratak deo opisa i datum objave.

## 4.4. Pretraga

- Polje za pretragu koje pretražuje događaje na osnovu teksta unetog u polje (traži se u naslovu i opisu događaja).
- Rezultat je paginiran i prikazuje se na način sličan listi događaja (naslov, kratak opis...).

## 4.5. Detaljan prikaz događaja

Klikom na naslov događaja (bilo da je sa home page, najposećenijih, kategorije ili pretrage), otvara se stranica koja prikazuje:

- Naslov, ceo opis, datum i vreme održavanja, lokaciju, datum kreiranja, ime autora (event creator), kategoriju, listu tagova.
- Broj pregleda (povećava se samo prvi put kada taj posetilac otvori događaj, identifikovano putem kolačića/sesije).
- Broj lajkova i dislajkova za događaj, uz mogućnost like ili dislike (jednom po korisniku/sesiji).
- (Opciono) Dugme „RSVP“ ako je max kapacitet != 0, i prikaz koliko je trenutno prijavljenih (ako se implementira sistem prijava).
- Sekciju sa komentarima (sortirani opadajuće po datumu). Svaki komentar ima ime autora, tekst, datum, i broj lajkova/dislajkova.
- Forma za dodavanje novog komentara (polja: ime autora komentara, tekst).
- Opcija like/dislike komentara – posetilac može jednom da lajkuje ili dislajkuje komentar (identifikovano kolačićima/sesijom).

## 4.6. Tagovi i „Pročitaj još...“

- Tag je link. Klikom na tag dobija se lista svih događaja sa tim tagom (naslov, kratak opis, kategorija, datum).
- Na dnu stranice detalja događaja, sekcija „Pročitaj još...“ prikazuje do 3 srodna događaja, koji dele bar jedan tag sa trenutnim događajem.

## 4.7. Blok „Najviše reakcija“

Na svakoj stranici javne platforme, u uglu, prikazati 3 događaja sa najviše reakcija (suma lajkova i dislajkova), sortiranih opadajuće. Naslovi su linkovi ka detaljnom prikazu.

# 5. Dodatni zahtevi

## 5.1. Skladištenje podataka

- Sve entitete i trenutna stanja čuvati u relacionoj bazi podataka.
- Moguće je, ali se kažnjava (minus 15 poena), čuvanje u memoriji aplikacije.

## 5.2. Upravljanje greškama i validacija

- Backend mora da validira svako kreiranje/izmenu entiteta (npr. da li je email već zauzet, da li su polja prazna, da li naziv kategorije već postoji, da li je max kapacitet < 0, itd.).
- U slučaju greške, korisniku se vraća odgovarajuća poruka, a radnja se ne obavlja.

## 5.3. Paginacija

Sve tabele i liste (kategorije, događaji, korisnici, komentari ako je potreban listing…) moraju biti paginirane.

Primer: prikaz 10 rezultata po stranici. Ako ih ima 25, to su 3 stranice (10, 10, 5). Na frontu treba obezbediti dugmad za prelazak na prethodnu i sledeću stranu.

## 5.4. Like/Dislike i view count

- Identifikacija posetioca zasniva se na kolačiću ili sesiji.
- Broj pregleda događaja (views) se uvećava samo prvi put kada taj posetilac otvori događaj.
- Like/dislike na događaj ili komentar se dozvoljava samo jednom po posetiocu.

## Paginacija (pojašnjenje)

Sve tabele i liste je potrebno da imaju paginaciju.

Paginacija je funkcionalnost gde ne prikazujemo sve rezultate na jednom mestu, već ih raspoređujemo po “stranicama”. Kroz stranice se prolazi sa dugmetom za prethodnu ili za sledeću stranu (ako postoje). Recimo da je limit 10 prikazanih rezultata, a u bazi postoji 25. Rezultat su 3 stranice (prve dve sa 10 i poslednja sa 5 rezultata) . Prelazak na drugu stranicu je novi zahtev koji sadrži broj željene stranice kao parametar, na osnovu koje će backend znati koji deo rezultata je potrebno dobiti iz baze: `offset (current_page - 1) * 10 limit 10`.

# 6. Tehnologije

Sistem treba da se sastoji iz dva dela:

- **Frontend:** JQuery, Vue.js, React ili Angular. Stilizovanje je opciono i moguće je korišćenje CSS framework-a po izboru.
- **Backend:** JAX-RS, NodeJS, ili SparkJava.
