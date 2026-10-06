# Sfida 52 Random

App Android per la sfida di risparmio a estrazione casuale.

- Imposti l'**obiettivo finale**, la **frequenza** (settimanale = 52 estrazioni, ogni 2 settimane = 26, mensile = 12) e la **data di inizio**.
- L'app crea importi proporzionali (1×, 2×, 3× … N×) che sommati danno esattamente l'obiettivo.
- A ogni scadenza estrai a caso uno degli importi rimasti. Vedi quali sono già usciti e quali sono ancora da estrarre.
- Segni ogni importo come **versato** quando hai messo davvero i soldi da parte.
- I dati restano salvati sul telefono.

## Ottenere l'APK

### Opzione A: GitHub (senza installare niente)
1. Crea un repository su github.com e carica tutto il contenuto di questa cartella (anche la cartella nascosta `.github`).
2. Apri la scheda **Actions**: la build "Build APK" parte da sola (circa 5 minuti).
3. A fine build, in fondo alla pagina scarica **Sfida52-apk** (è uno zip con dentro l'APK).
4. Copia l'APK sul telefono e aprilo. Android chiederà di consentire l'installazione da questa fonte.

### Opzione B: Android Studio
1. Installa Android Studio e apri questa cartella con **File › Open**.
2. Attendi la sincronizzazione di Gradle.
3. Collega il telefono (con debug USB attivo) e premi **Run**, oppure **Build › Build APK(s)**.

## Struttura
- `Model.kt`: dati della sfida e calcolo degli importi
- `Storage.kt`: salvataggio sul telefono
- `ChallengeViewModel.kt`: estrazione casuale e aggiornamenti
- `MainActivity.kt`: schermate (impostazione e sfida)
