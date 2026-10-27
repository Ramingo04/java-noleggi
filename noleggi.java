import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Random;


// CLASSE PRINCIPALE
public class Main {

    public static void main(String[] args) {

        ArchivioVeicoli archivioVeicoli = new ArchivioVeicoli();
        ArchivioUtenti archivioUtenti = new ArchivioUtenti();

        // Creazione dei veicoli
        Auto auto = new Auto(
                "Fiat", 1001, LocalDate.of(2022, 5, 10),
                "AB123CD", 5, 1400, 50
        );

        Bici bici = new Bici(
                "Bianchi", 1002, LocalDate.of(2023, 3, 15),
                "city"
        );

        Motocicletta moto = new Motocicletta(
                "Honda", 1003, LocalDate.of(2021, 7, 20)
        );

        FurgoneMerci furgoneMerci = new FurgoneMerci(
                "Iveco", 1004, LocalDate.of(2020, 4, 12),
                "IL789MN", 8
        );

        FurgonePersone furgonePersone = new FurgonePersone(
                "Mercedes", 1005, LocalDate.of(2022, 9, 1),
                "OP123QR", 9
        );

        // Inserimento dei veicoli nell'archivio.
        // La traccia richiede di gestire i valori non corretti.
        try {
            archivioVeicoli.inserisciVeicolo(auto);
            archivioVeicoli.inserisciVeicolo(bici);
            archivioVeicoli.inserisciVeicolo(moto);
            archivioVeicoli.inserisciVeicolo(furgoneMerci);
            archivioVeicoli.inserisciVeicolo(furgonePersone);
        } catch (VeicoloNonValidoException e) {
            System.out.println(e.getMessage());
        }

        // Visualizzazione dei veicoli.
        archivioVeicoli.stampaVeicoli();

        // Ricerca di auto per cilindrata e costo massimo giornaliero.
        ArrayList<Auto> autoTrovate =
                archivioVeicoli.cercaAuto(1400, 60);

        System.out.println("Auto trovate:");
        for (Auto a : autoTrovate) {
            System.out.println(a);
        }

        // Noleggio di un veicolo identificato dal VIN.
        Utente utente = new Utente("Mario", "mario@email.it");

        try {
            archivioVeicoli.noleggiaVeicolo(
                    1001, utente, 3, archivioUtenti
            );
        } catch (VeicoloNonDisponibileException
                 | VeicoloNonTrovatoException e) {
            System.out.println(e.getMessage());
        }

        // Visualizzazione dei veicoli attualmente noleggiati.
        archivioVeicoli.stampaVeicoliNoleggiati();

        // Visualizzazione dei noleggi registrati nell'archivio utenti.
        archivioUtenti.stampaNoleggi();

        // Restituzione del veicolo.
        try {
            archivioVeicoli.restituisciVeicolo(
                    1001, archivioUtenti
            );
        } catch (VeicoloNonNoleggiatoException
                 | VeicoloNonTrovatoException e) {
            System.out.println(e.getMessage());
        }

        // Esempio della parte concorrente sull'archivio degli utenti.
        // SIZE è il limite superiore dei numeri casuali usati nei VIN,
        // non la capacità massima della lista.
        int SIZE = 10;

        Thread produttore = new Thread(
                new Produttore(archivioUtenti, 5, SIZE)
        );

        Thread consumatore = new Thread(
                new Consumatore(archivioUtenti, 5)
        );

        Thread lettore = new Thread(
                new Lettore(archivioUtenti, 5)
        );

        produttore.start();
        consumatore.start();
        lettore.start();

        try {
            produttore.join();
            consumatore.join();
            lettore.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}


// INTERFACCIA PER I VEICOLI NOLEGGIABILI
interface Noleggiabile {

    boolean noleggiaVeicolo(Utente utente, int giorni);

    Noleggio statoNoleggio();

    void pagaNoleggio();

    void restituisciVeicolo();

    double getCostoGiornaliero();
}


// INTERFACCIA PER I VEICOLI CHE TRASPORTANO PASSEGGERI
interface TrasportoPasseggeri {

    int capienza_passeggeri();
}


// CLASSE ASTRATTA VEICOLO
// Tutti i veicoli condividono gli attributi comuni e sono noleggiabili.
abstract class Veicolo implements Noleggiabile {

    private String marca;
    private final int vin;
    private LocalDate dataImmissione;

    // Il noleggio attuale è null quando il veicolo è disponibile.
    private Noleggio noleggioAttuale;

    public Veicolo(
            String marca,
            int vin,
            LocalDate dataImmissione
    ) {
        this.marca = marca;
        this.vin = vin;
        this.dataImmissione = dataImmissione;
        this.noleggioAttuale = null;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public int getVin() {
        return vin;
    }

    public LocalDate getDataImmissione() {
        return dataImmissione;
    }

    public void setDataImmissione(LocalDate dataImmissione) {
        this.dataImmissione = dataImmissione;
    }

    // Controllo degli attributi comuni, effettuato quando il veicolo
    // viene inserito nell'archivio.
    public void verificaAttributi() throws VeicoloNonValidoException {
        if (marca == null || marca.isEmpty()
                || vin <= 0
                || dataImmissione == null) {
            throw new VeicoloNonValidoException(
                    "Attributi del veicolo non validi."
            );
        }
    }

    @Override
    public boolean noleggiaVeicolo(Utente utente, int giorni) {

        // La traccia consente il noleggio solo se il veicolo è libero.
        if (noleggioAttuale != null) {
            return false;
        }

        double costoTotale = getCostoGiornaliero() * giorni;

        noleggioAttuale = new Noleggio(
                utente, String.valueOf(vin), giorni, costoTotale
        );

        return true;
    }

    @Override
    public Noleggio statoNoleggio() {
        return noleggioAttuale;
    }

    @Override
    public void pagaNoleggio() {
        if (noleggioAttuale != null) {
            noleggioAttuale.paga();
        }
    }

    @Override
    public void restituisciVeicolo() {
        noleggioAttuale = null;
    }

    @Override
    public String toString() {
        return "Marca: " + marca
                + ", VIN: " + vin
                + ", Data immissione: " + dataImmissione;
    }
}


// CLASSE UTENTE
class Utente {

    private String nome;
    private String email;

    public Utente(String nome, String email) {
        this.nome = nome;
        this.email = email;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "Nome: " + nome + ", E-mail: " + email;
    }
}


// CLASSE NOLEGGIO
class Noleggio {

    private Utente utente;
    private String vin;
    private int numGiorni;
    private double costoTotale;
    private boolean pagato;

    public Noleggio(
            Utente utente,
            String vin,
            int numGiorni,
            double costoTotale
    ) {
        this.utente = utente;
        this.vin = vin;
        this.numGiorni = numGiorni;
        this.costoTotale = costoTotale;
        this.pagato = false;
    }

    public Utente getUtente() {
        return utente;
    }

    public String getVin() {
        return vin;
    }

    public int getNumGiorni() {
        return numGiorni;
    }

    public double getCostoTotale() {
        return costoTotale;
    }

    public boolean isPagato() {
        return pagato;
    }

    public void paga() {
        pagato = true;
    }

    @Override
    public String toString() {
        return "Noleggio: " + utente
                + ", VIN: " + vin
                + ", Giorni: " + numGiorni
                + ", Costo totale: " + costoTotale
                + ", Pagato: " + pagato;
    }
}


// CLASSE BICI
class Bici extends Veicolo {

    private String modello;

    public Bici(
            String marca,
            int vin,
            LocalDate dataImmissione,
            String modello
    ) {
        super(marca, vin, dataImmissione);
        this.modello = modello;
    }

    public String getModello() {
        return modello;
    }

    public void setModello(String modello) {
        this.modello = modello;
    }

    @Override
    public double getCostoGiornaliero() {
        if (modello.equals("city")) {
            return 20;
        } else {
            // Mountain bike ed elettrica costano entrambe 30 euro.
            return 30;
        }
    }

    @Override
    public void verificaAttributi() throws VeicoloNonValidoException {
        super.verificaAttributi();

        if (!"city".equals(modello)
                && !"mountain bike".equals(modello)
                && !"elettrica".equals(modello)) {
            throw new VeicoloNonValidoException(
                    "Modello di bicicletta non valido."
            );
        }
    }

    @Override
    public String toString() {
        return "Bici: " + super.toString()
                + ", Modello: " + modello
                + ", Costo giornaliero: " + getCostoGiornaliero();
    }
}


// CLASSE MOTOCICLETTA
// La traccia non specifica attributi o tariffe aggiuntivi per la moto.
class Motocicletta extends Veicolo {

    public Motocicletta(
            String marca,
            int vin,
            LocalDate dataImmissione
    ) {
        super(marca, vin, dataImmissione);
    }

    @Override
    public double getCostoGiornaliero() {
        // La traccia non assegna una tariffa alla motocicletta.
        return 0.0;
    }

    @Override
    public String toString() {
        return "Motocicletta: " + super.toString();
    }
}


// CLASSE AUTO
class Auto extends Veicolo implements TrasportoPasseggeri {

    private String targa;
    private int numPosti;
    private double cilindrata;
    private double costoGiornaliero;

    public Auto(
            String marca,
            int vin,
            LocalDate dataImmissione,
            String targa,
            int numPosti,
            double cilindrata,
            double costoGiornaliero
    ) {
        super(marca, vin, dataImmissione);
        this.targa = targa;
        this.numPosti = numPosti;
        this.cilindrata = cilindrata;
        this.costoGiornaliero = costoGiornaliero;
    }

    public String getTarga() {
        return targa;
    }

    public void setTarga(String targa) {
        this.targa = targa;
    }

    public int getNumPosti() {
        return numPosti;
    }

    public void setNumPosti(int numPosti) {
        this.numPosti = numPosti;
    }

    public double getCilindrata() {
        return cilindrata;
    }

    public void setCilindrata(double cilindrata) {
        this.cilindrata = cilindrata;
    }

    @Override
    public double getCostoGiornaliero() {
        return costoGiornaliero;
    }

    public void setCostoGiornaliero(double costoGiornaliero) {
        this.costoGiornaliero = costoGiornaliero;
    }

    @Override
    public int capienza_passeggeri() {
        return numPosti;
    }

    @Override
    public void verificaAttributi() throws VeicoloNonValidoException {
        super.verificaAttributi();

        if (numPosti <= 0
                || cilindrata <= 0
                || costoGiornaliero <= 0) {
            throw new VeicoloNonValidoException(
                    "Attributi dell'auto non validi."
            );
        }
    }

    @Override
    public String toString() {
        return "Auto: " + super.toString()
                + ", Targa: " + targa
                + ", Posti: " + numPosti
                + ", Cilindrata: " + cilindrata
                + ", Costo giornaliero: " + costoGiornaliero;
    }
}


// CLASSE FURGONE PER IL TRASPORTO MERCI
class FurgoneMerci extends Veicolo {

    private String targa;
    private double cubatura;

    public FurgoneMerci(
            String marca,
            int vin,
            LocalDate dataImmissione,
            String targa,
            double cubatura
    ) {
        super(marca, vin, dataImmissione);
        this.targa = targa;
        this.cubatura = cubatura;
    }

    public String getTarga() {
        return targa;
    }

    public void setTarga(String targa) {
        this.targa = targa;
    }

    public double getCubatura() {
        return cubatura;
    }

    public void setCubatura(double cubatura) {
        this.cubatura = cubatura;
    }

    @Override
    public double getCostoGiornaliero() {
        return cubatura * 7;
    }

    @Override
    public void verificaAttributi() throws VeicoloNonValidoException {
        super.verificaAttributi();

        if (cubatura <= 0) {
            throw new VeicoloNonValidoException(
                    "Cubatura del furgone non valida."
            );
        }
    }

    @Override
    public String toString() {
        return "Furgone merci: " + super.toString()
                + ", Targa: " + targa
                + ", Cubatura: " + cubatura
                + ", Costo giornaliero: " + getCostoGiornaliero();
    }
}


// CLASSE FURGONE PER IL TRASPORTO PERSONE
class FurgonePersone extends Veicolo
        implements TrasportoPasseggeri {

    private String targa;
    private int numPosti;

    public FurgonePersone(
            String marca,
            int vin,
            LocalDate dataImmissione,
            String targa,
            int numPosti
    ) {
        super(marca, vin, dataImmissione);
        this.targa = targa;
        this.numPosti = numPosti;
    }

    public String getTarga() {
        return targa;
    }

    public void setTarga(String targa) {
        this.targa = targa;
    }

    public int getNumPosti() {
        return numPosti;
    }

    public void setNumPosti(int numPosti) {
        this.numPosti = numPosti;
    }

    @Override
    public int capienza_passeggeri() {
        return numPosti;
    }

    @Override
    public double getCostoGiornaliero() {
        return numPosti * 10;
    }

    @Override
    public void verificaAttributi() throws VeicoloNonValidoException {
        super.verificaAttributi();

        if (numPosti <= 0) {
            throw new VeicoloNonValidoException(
                    "Numero di posti del furgone non valido."
            );
        }
    }

    @Override
    public String toString() {
        return "Furgone persone: " + super.toString()
                + ", Targa: " + targa
                + ", Posti: " + numPosti
                + ", Costo giornaliero: " + getCostoGiornaliero();
    }
}


// ECCEZIONE PER ATTRIBUTI NON VALIDI O VIN DUPLICATO
class VeicoloNonValidoException extends Exception {

    public VeicoloNonValidoException(String messaggio) {
        super(messaggio);
    }
}


// ECCEZIONE PER VEICOLO NON DISPONIBILE
class VeicoloNonDisponibileException extends Exception {

    public VeicoloNonDisponibileException(String messaggio) {
        super(messaggio);
    }
}


// ECCEZIONE PER VEICOLO NON TROVATO
class VeicoloNonTrovatoException extends Exception {

    public VeicoloNonTrovatoException(String messaggio) {
        super(messaggio);
    }
}


// ECCEZIONE PER RESTITUZIONE DI UN VEICOLO NON NOLEGGIATO
class VeicoloNonNoleggiatoException extends Exception {

    public VeicoloNonNoleggiatoException(String messaggio) {
        super(messaggio);
    }
}


// ARCHIVIO DEI VEICOLI
class ArchivioVeicoli {

    // La traccia modificata richiede ArrayList al posto della Map.
    private ArrayList<Veicolo> veicoli = new ArrayList<>();

    public void inserisciVeicolo(Veicolo veicolo)
            throws VeicoloNonValidoException {

        // Non si possono inserire due veicoli con lo stesso VIN.
        if (cercaVeicolo(veicolo.getVin()) != null) {
            throw new VeicoloNonValidoException(
                    "Esiste già un veicolo con questo VIN."
            );
        }

        // Controlla gli attributi previsti dalle tipologie concrete.
        veicolo.verificaAttributi();

        veicoli.add(veicolo);
    }

    // Ricerca lineare tramite VIN, come richiesto dalla traccia.
    public Veicolo cercaVeicolo(int vin) {
        for (Veicolo veicolo : veicoli) {
            if (veicolo.getVin() == vin) {
                return veicolo;
            }
        }

        return null;
    }

    // Visualizza tutti i veicoli.
    // Il toString della classe concreta stampa i dati specifici.
    public void stampaVeicoli() {
        for (Veicolo veicolo : veicoli) {
            System.out.println(veicolo);
        }
    }

    // Cerca le auto con la cilindrata richiesta e costo non superiore
    // al costo massimo indicato.
    public ArrayList<Auto> cercaAuto(
            double cilindrata,
            double costoMassimo
    ) {
        ArrayList<Auto> risultato = new ArrayList<>();

        for (Veicolo veicolo : veicoli) {
            if (veicolo instanceof Auto) {
                Auto auto = (Auto) veicolo;

                if (auto.getCilindrata() == cilindrata
                        && auto.getCostoGiornaliero() <= costoMassimo) {
                    risultato.add(auto);
                }
            }
        }

        return risultato;
    }

    public void noleggiaVeicolo(
            int vin,
            Utente utente,
            int giorni,
            ArchivioUtenti archivioUtenti
    ) throws VeicoloNonDisponibileException,
             VeicoloNonTrovatoException {

        Veicolo veicolo = cercaVeicolo(vin);

        if (veicolo == null) {
            throw new VeicoloNonTrovatoException(
                    "Veicolo non presente nell'archivio."
            );
        }

        // Tutte le sottoclassi di Veicolo implementano Noleggiabile
        // tramite la classe astratta Veicolo.
        boolean effettuato = veicolo.noleggiaVeicolo(utente, giorni);

        if (!effettuato) {
            throw new VeicoloNonDisponibileException(
                    "Il veicolo è già noleggiato."
            );
        }

        // Si registra nell'archivio utenti lo stesso oggetto Noleggio
        // creato dal veicolo.
        Noleggio noleggio = veicolo.statoNoleggio();
        archivioUtenti.registraNoleggio(noleggio);

        System.out.println(
                "Costo totale del noleggio: "
                        + noleggio.getCostoTotale() + " euro"
        );
    }

    public void restituisciVeicolo(
            int vin,
            ArchivioUtenti archivioUtenti
    ) throws VeicoloNonNoleggiatoException,
             VeicoloNonTrovatoException {

        Veicolo veicolo = cercaVeicolo(vin);

        if (veicolo == null) {
            throw new VeicoloNonTrovatoException(
                    "Veicolo non presente nell'archivio."
            );
        }

        Noleggio noleggio = veicolo.statoNoleggio();

        // La traccia richiede di gestire il caso in cui il veicolo
        // non sia attualmente noleggiato.
        if (noleggio == null) {
            throw new VeicoloNonNoleggiatoException(
                    "Il veicolo non è attualmente noleggiato."
            );
        }

        veicolo.restituisciVeicolo();

        // Il noleggio viene rimosso dall'archivio degli utenti tramite VIN.
        archivioUtenti.rimuoviPerVIN(String.valueOf(vin));
    }

    // Visualizza esclusivamente i veicoli noleggiati.
    public void stampaVeicoliNoleggiati() {
        for (Veicolo veicolo : veicoli) {
            if (veicolo.statoNoleggio() != null) {
                System.out.println(veicolo);
                System.out.println(veicolo.statoNoleggio());
            }
        }
    }
}


// ARCHIVIO DEGLI UTENTI
class ArchivioUtenti {

    // La lista contiene direttamente i noleggi in corso.
    private ArrayList<Noleggio> noleggi = new ArrayList<>();

    // Registra un noleggio.
    public synchronized void registraNoleggio(Noleggio noleggio) {
        noleggi.add(noleggio);
        notifyAll();
    }

    // Cerca il primo noleggio associato all'e-mail.
    public synchronized Noleggio cercaPerEmail(String email) {
        for (Noleggio noleggio : noleggi) {
            if (noleggio.getUtente().getEmail().equals(email)) {
                return noleggio;
            }
        }

        return null;
    }

    // Cerca un noleggio tramite VIN.
    public synchronized Noleggio cercaPerVIN(String vin) {
        for (Noleggio noleggio : noleggi) {
            if (noleggio.getVin().equals(vin)) {
                return noleggio;
            }
        }

        return null;
    }

    // Rimuove il noleggio relativo al veicolo restituito.
    public synchronized void rimuoviPerVIN(String vin) {
        for (int i = 0; i < noleggi.size(); i++) {
            if (noleggi.get(i).getVin().equals(vin)) {
                noleggi.remove(i);
                notifyAll();
                return;
            }
        }
    }

    // Verifica se un VIN è già presente.
    public synchronized boolean contieneVIN(String vin) {
        for (Noleggio noleggio : noleggi) {
            if (noleggio.getVin().equals(vin)) {
                return true;
            }
        }

        return false;
    }

    // Inserimento atomico per il produttore:
    // controllo del VIN e inserimento avvengono sotto lo stesso lock.
    public synchronized boolean inserisciSeVINLibero(Noleggio noleggio) {
        if (contieneVIN(noleggio.getVin())) {
            return false;
        }

        noleggi.add(noleggio);
        notifyAll();
        return true;
    }

    // Estrazione usata dal consumatore.
    // Se la lista è vuota, il thread attende un inserimento.
    public synchronized Noleggio estraiPrimo()
            throws InterruptedException {

        while (noleggi.isEmpty()) {
            wait();
        }

        Noleggio noleggio = noleggi.remove(0);

        notifyAll();

        return noleggio;
    }

    // Lettura sincronizzata di tutti i noleggi correnti.
    public synchronized void stampaNoleggi() {
        for (Noleggio noleggio : noleggi) {
            System.out.println(noleggio);
        }
    }
}


// THREAD PRODUTTORE
class Produttore implements Runnable {

    private ArchivioUtenti archivio;
    private int numeroInserimenti;
    private int SIZE;
    private Random random = new Random();

    public Produttore(
            ArchivioUtenti archivio,
            int numeroInserimenti,
            int SIZE
    ) {
        this.archivio = archivio;
        this.numeroInserimenti = numeroInserimenti;
        this.SIZE = SIZE;
    }

    @Override
    public void run() {

        for (int i = 1; i <= numeroInserimenti; i++) {

            // E-mail costruita con il progressivo richiesto dalla traccia.
            String email = "user" + i + "@gmail.com";
            Utente utente = new Utente("User " + i, email);

            boolean inserito = false;

            while (!inserito) {

                // VIN composto da "vin" e numero casuale tra 1 e SIZE.
                String vin = "vin" + (random.nextInt(SIZE) + 1);

                // Il noleggio simulato serve a esercitare la concorrenza.
                // La traccia non specifica qui una tariffa.
                Noleggio noleggio = new Noleggio(
                        utente, vin, 1, 0.0
                );

                // Se il VIN esiste già, si genera un altro numero.
                inserito = archivio.inserisciSeVINLibero(noleggio);

                if (inserito) {
                    System.out.println(
                            "Produttore: inserito " + noleggio
                    );
                }
            }
        }
    }
}


// THREAD CONSUMATORE
class Consumatore implements Runnable {

    private ArchivioUtenti archivio;
    private int numeroEstrazioni;

    public Consumatore(
            ArchivioUtenti archivio,
            int numeroEstrazioni
    ) {
        this.archivio = archivio;
        this.numeroEstrazioni = numeroEstrazioni;
    }

    @Override
    public void run() {

        for (int i = 0; i < numeroEstrazioni; i++) {
            try {
                // Estrae e rimuove un noleggio dalla lista condivisa.
                Noleggio noleggio = archivio.estraiPrimo();

                System.out.println(
                        "Consumatore: restituito " + noleggio
                );

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }
}


// THREAD LETTORE
class Lettore implements Runnable {

    private ArchivioUtenti archivio;
    private int numeroLetture;

    public Lettore(ArchivioUtenti archivio, int numeroLetture) {
        this.archivio = archivio;
        this.numeroLetture = numeroLetture;
    }

    @Override
    public void run() {

        for (int i = 0; i < numeroLetture; i++) {

            System.out.println("Noleggi attualmente presenti:");
            archivio.stampaNoleggi();

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }
}
