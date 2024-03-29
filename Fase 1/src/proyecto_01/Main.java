package proyecto_01;

import proyecto_01.Structures.Pile;
import proyecto_01.Structures.List;
import proyecto_01.Structures.DoubleList;
import proyecto_01.Structures.Nodes.NodeV;
import proyecto_01.Structures.Nodes.NodeC;
import proyecto_01.Structures.Cola;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;
import java.util.Scanner;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import proyecto_01.Structures.Nodes.NodeDoubleC;
import proyecto_01.Structures.Nodes.NodeImage;

/**
 *
 * @author Alexis
 */
public class Main {

    public static List Ventanillas = new List();
    public static List Attended = new List();
    public static DoubleList Waiting = new DoubleList();
    public static Cola Clientes = new Cola();
    public static Cola PrinterC = new Cola();
    public static Cola PrinterBW = new Cola();
    public static int globalStep = 0;
    public static int lastId = 0;
    public static boolean play = true;

    public static void main(String[] args) {
        menu();
    }

    public static String openFileChooser() {
        String aux = "";
        String text = "";
        JFileChooser file;
        File open;
        FileReader files = null;
        BufferedReader read = null;
        try {
            file = new JFileChooser();
            file.showOpenDialog(new JFrame());
            open = file.getSelectedFile();
            if (open != null) {
                files = new FileReader(open);
                read = new BufferedReader(files);
                while ((aux = read.readLine()) != null) {
                    text += aux + "\n";
                }
                files.close();
                read.close();
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, ex + ""
                    + "\nNo se ha encontrado el archivo",
                    "Información", JOptionPane.WARNING_MESSAGE);
        } finally {
            try {
                files.close();
            } catch (IOException ex) {
                System.out.println(ex);
            }
            try {
                read.close();
            } catch (IOException ex) {
                System.out.println(ex);
            }
        }
        return text;
    }

    public static void loadJson() {
        String fileContent = openFileChooser();
        try {
            JsonParser parser = new JsonParser();
            JsonObject jObject = (JsonObject) parser.parse(fileContent);
            int cont = 1;
            while (jObject.get("Cliente" + cont) != null) {
                cont += 1;
            }
            cont -= 1;
            for (int i = 0; i < cont; i++) {
                JsonElement client = jObject.get("Cliente" + (i + 1));
                JsonObject data = client.getAsJsonObject();
                int id = data.get("id_cliente").getAsInt();
                String name = data.get("nombre_cliente").getAsString();
                int imgC = data.get("img_color").getAsInt();
                int imgBW = data.get("img_bw").getAsInt();
                int contC = 0;
                List imagesC = new List();
                while (contC == imgC) {
                    imagesC.addImage(new Image(id, "C", 0));
                    contC++;
                }
                int contBW = 0;
                List imagesBW = new List();
                while (contBW == imgBW) {
                    imagesBW.addImage(new Image(id, "BW", 0));
                    contBW++;
                }
                if (imgC == 0 || imgBW == 0) {
                    Cliente newClient = new Cliente(id, name, (imgC + 1), (imgBW + 1), imagesC, imagesBW, false, false, false, 0, "");
                    lastId = id;
                    Clientes.addC(newClient);
                } else {
                    Cliente newClient = new Cliente(id, name, imgC, imgBW, imagesC, imagesBW, false, false, false, 0, "");
                    lastId = id;
                    Clientes.addC(newClient);
                }
            }
            System.out.println("Archivo leido con éxito");
        } catch (Exception e) {
            System.out.println("Información: El archivo no es un Json");
        } finally {
        }
    }

    public static void vGenerator() {
        Scanner s = new Scanner(System.in);
        System.out.print("Ingrese la cantidad de ventanillas: ");
        int num = s.nextInt();
        for (int i = 0; i < num; i++) {
            Ventanillas.addV(new Ventanilla((i + 1), ("Ventanilla " + (i + 1)), null, new Pile()));
        }
    }

    public static void simulation() {

        boolean Active = true;
        while (Active) {
            System.out.println("\n");
            System.out.println("------------------------ SALTO NO." + (globalStep + 1) + " ------------------------");
            System.out.println("......................... ACCIONES .........................");

            //Generar clientes random
            cGenerator();

            //Asignar cliente a ventanilla
            NodeV CurrentV = Ventanillas.first;
            while (CurrentV != null) {
                if (CurrentV.value.getClient() == null) {
                    if (Clientes.getSizeC() == 0) {
                        break;
                    }
                    NodeC client = Clientes.getAndRemoveC();
                    CurrentV.value.setClient(client);
                    System.out.println("El cliente " + CurrentV.value.getClient().value.getName() + " ingresa a " + CurrentV.value.getName());
                    CurrentV.value.getClient().value.setgiveImg(true);
                    NodeV CurrentAll = Ventanillas.first;
                    while (CurrentAll.next != null) {
                        if (CurrentV.next != null) {
                            if (CurrentV.next.value.getId() == CurrentAll.next.value.getId()) {
                                if (CurrentV.next.value.getClient() != null) {
                                    CurrentV.next.value.getClient().value.setgiveImg(false);
                                }
                            }
                        }
                        CurrentAll = CurrentAll.next;
                    }
                    break;
                } else {
                    CurrentV.value.getClient().value.setgiveImg(false);
                    CurrentV = CurrentV.next;
                }
            }
            //Clientes en ventanilla dan imagenes
            NodeV CurrentV2 = Ventanillas.first;
            while (CurrentV2 != null) {
                if (CurrentV2.value.getClient() != null) {
                    if (CurrentV2.value.getClient().value.getgiveImg() != true) {
                        Pile images = CurrentV2.value.getImages();
                        //Cantidad de imagenes que tiene el cliente en ventanilla
                        int numC = CurrentV2.value.getClient().value.getnumImgC(); //0
                        int numBW = CurrentV2.value.getClient().value.getnumImgBW(); //1
                        //int cantImages = numBW + numC; //1

                        //Cantidad de imagenes que tiene la ventanilla
                        int numPileC = CurrentV2.value.getImages().getCNumber();//0
                        int numPileBW = CurrentV2.value.getImages().getBWNumber();//0
                        if (numC != 0) {
                            //2  != 3
                            if (numPileC != numC) {
                                images.add(new Image(CurrentV2.value.getClient().value.getId(), "C", 0));
                                CurrentV2.value.getClient().value.getImgC().removeImage();
                                System.out.println("La " + CurrentV2.value.getName() + " recibe una imagen a color del cliente "
                                        + CurrentV2.value.getClient().value.getName());
                            } else {
                                if (numBW != 0) {
                                    if (numPileBW != numBW) {
                                        images.add(new Image(CurrentV2.value.getClient().value.getId(), "BW", 0));
                                        CurrentV2.value.getClient().value.getImgBW().removeImage();
                                        System.out.println("La " + CurrentV2.value.getName() + " recibe una imagen en BW del cliente "
                                                + CurrentV2.value.getClient().value.getName());
                                    } else {
                                        NodeC clientWaiter = CurrentV2.value.getClient();
                                        System.out.println("El cliente " + clientWaiter.value.getName() + " es atendido e ingresa a la lista de espera");
                                        System.out.println("La " + CurrentV2.value.getName() + " envía las imágenes del cliente "
                                                + clientWaiter.value.getName() + " a sus respectivas colas de impresión");
                                        Cliente clientAux = new Cliente(clientWaiter.value.getId(), clientWaiter.value.getName(),
                                                clientWaiter.value.getnumImgC(), clientWaiter.value.getnumImgBW(), new List(),
                                                new List(), false, true, true, 0, CurrentV2.value.getName());
                                        if (Clientes.getSizeC() == 0) {
                                            CurrentV2.value.setClient(null);
                                        } else {
                                            NodeC inQueueC = Clientes.getAndRemoveC();
                                            System.out.println("El cliente " + inQueueC.value.getName() + " ingresa a " + CurrentV2.value.getName());
                                            CurrentV2.value.setClient(inQueueC);
                                        }
                                        //Enviar imagenes de ventanilla a cola de impresion
                                        NodeImage CurrentImg = images.first;
                                        while (CurrentImg != null) {
                                            if ("C".equals(CurrentImg.value.getType())) {
                                                PrinterC.addImg(new Image(clientWaiter.value.getId(), "C", 0));
                                            } else {
                                                PrinterBW.addImg(new Image(clientWaiter.value.getId(), "BW", 0));
                                            }
                                            CurrentImg = CurrentImg.next;
                                        }
                                        CurrentV2.value.getImages().cleanPile();
                                        Waiting.add(clientAux);
                                    }
                                } else {
                                    NodeC clientWaiter = CurrentV2.value.getClient();
                                    System.out.println("El cliente " + clientWaiter.value.getName() + " es atendido e ingresa a la lista de espera");
                                    System.out.println("La " + CurrentV2.value.getName() + " envía las imágenes del cliente "
                                            + clientWaiter.value.getName() + " a sus respectivas colas de impresión");
                                    Cliente clientAux = new Cliente(clientWaiter.value.getId(), clientWaiter.value.getName(),
                                            clientWaiter.value.getnumImgC(), clientWaiter.value.getnumImgBW(), new List(),
                                            new List(), false, true, true, 0, CurrentV2.value.getName());
                                    if (Clientes.getSizeC() == 0) {
                                        CurrentV2.value.setClient(null);
                                    } else {
                                        NodeC inQueueC = Clientes.getAndRemoveC();
                                        System.out.println("El cliente " + inQueueC.value.getName() + " ingresa a " + CurrentV2.value.getName());
                                        CurrentV2.value.setClient(inQueueC);
                                    }
                                    //Enviar imagenes de ventanilla a cola de impresion
                                    NodeImage CurrentImg = images.first;
                                    while (CurrentImg != null) {
                                        if ("C".equals(CurrentImg.value.getType())) {
                                            PrinterC.addImg(new Image(clientWaiter.value.getId(), "C", 0));
                                        } else {
                                            PrinterBW.addImg(new Image(clientWaiter.value.getId(), "BW", 0));
                                        }
                                        CurrentImg = CurrentImg.next;
                                    }
                                    CurrentV2.value.getImages().cleanPile();
                                    Waiting.add(clientAux);
                                }
                            }
                        } else if (numBW != 0) {
                            if (numPileBW != numBW) {
                                images.add(new Image(CurrentV2.value.getClient().value.getId(), "BW", 0));
                                CurrentV2.value.getClient().value.getImgBW().removeImage();
                                System.out.println("La " + CurrentV2.value.getName() + " recibe una imagen en BW del cliente "
                                        + CurrentV2.value.getClient().value.getName());
                            } else {
                                NodeC clientWaiter = CurrentV2.value.getClient();
                                System.out.println("El cliente " + clientWaiter.value.getName() + " es atendido e ingresa a la lista de espera");
                                System.out.println("La " + CurrentV2.value.getName() + " envía las imágenes del cliente "
                                        + clientWaiter.value.getName() + " a sus respectivas colas de impresión");
                                Cliente clientAux = new Cliente(clientWaiter.value.getId(), clientWaiter.value.getName(),
                                        clientWaiter.value.getnumImgC(), clientWaiter.value.getnumImgBW(), new List(),
                                        new List(), false, true, true, 0, CurrentV2.value.getName());
                                if (Clientes.getSizeC() == 0) {
                                    CurrentV2.value.setClient(null);
                                } else {
                                    NodeC inQueueC = Clientes.getAndRemoveC();
                                    System.out.println("El cliente " + inQueueC.value.getName() + " ingresa a " + CurrentV2.value.getName());
                                    CurrentV2.value.setClient(inQueueC);
                                }
                                //Enviar imagenes de ventanilla a cola de impresion
                                NodeImage CurrentImg = images.first;
                                while (CurrentImg != null) {
                                    if ("C".equals(CurrentImg.value.getType())) {
                                        PrinterC.addImg(new Image(clientWaiter.value.getId(), "C", 0));
                                    } else {
                                        PrinterBW.addImg(new Image(clientWaiter.value.getId(), "BW", 0));
                                    }
                                    CurrentImg = CurrentImg.next;
                                }
                                CurrentV2.value.getImages().cleanPile();
                                Waiting.add(clientAux);
                            }
                        }
                    }
                }
                CurrentV2 = CurrentV2.next;
            }

            //Agregar un paso al contador de los clientes
            NodeDoubleC CurrentStep = Waiting.first;
            while (CurrentStep != null) {
                if (CurrentStep.value.getWait() != true) {
                    int auxStepCont = CurrentStep.value.getStepCont();
                    CurrentStep.value.setStepCont(auxStepCont += 1);
                }
                CurrentStep = CurrentStep.next;
            }

            //Impresoras regresan imagenes a clientes
            boolean sameStepC = false;
            boolean sameStepBW = false;
            NodeDoubleC CurrentV3 = Waiting.first;
            while (CurrentV3 != null) {
                if (CurrentV3.value.getWait() != true) {
                    //Recorre cada cola de impresion en busca del mismo id
                    //COLOR
                    NodeImage CurrentC = PrinterC.first2;
                    if (CurrentC != null) {
                        if (CurrentV3.value.getId() == CurrentC.value.getIdClient()) {
                            if (sameStepC == false) {
                                if (CurrentC.value.getStep() == 2) {
                                    System.out.println("Se completa la impresión de una imagen a color y se le"
                                            + " entrega al cliente " + CurrentV3.value.getName());
                                    List imgListC = CurrentV3.value.getImgC();
                                    imgListC.addImage(new Image(CurrentC.value.getIdClient(), CurrentC.value.getType(), CurrentC.value.getStep()));
                                    CurrentV3.value.setImgC(imgListC);
                                    PrinterC.removeImage();
                                    sameStepC = true;
                                } else {
                                    int stepNum = CurrentC.value.getStep();
                                    CurrentC.value.setStep(stepNum += 1);
                                    if (CurrentC.value.getStep() == 2) {
                                        System.out.println("Se completa la impresión de una imagen a color y se le"
                                                + " entrega al cliente " + CurrentV3.value.getName());
                                        List imgListC = CurrentV3.value.getImgC();
                                        imgListC.addImage(new Image(CurrentC.value.getIdClient(), CurrentC.value.getType(), CurrentC.value.getStep()));
                                        CurrentV3.value.setImgC(imgListC);
                                        PrinterC.removeImage();
                                        sameStepC = true;
                                    }
                                }
                            }
                        }
                    }

                    //BW
                    NodeImage CurrentC2 = PrinterBW.first2;
                    if (CurrentC2 != null) {
                        if (CurrentV3.value.getId() == CurrentC2.value.getIdClient()) {
                            if (sameStepBW == false) {
                                if (CurrentC2.value.getStep() == 1) {
                                    System.out.println("Se completa la impresión de una imagen en blanco y negro y se le"
                                            + " entrega al cliente " + CurrentV3.value.getName());
                                    List imgListBW = CurrentV3.value.getImgBW();
                                    imgListBW.addImage(new Image(CurrentC2.value.getIdClient(), CurrentC2.value.getType(), CurrentC2.value.getStep()));
                                    CurrentV3.value.setImgBW(imgListBW);
                                    PrinterBW.removeImage();
                                    sameStepBW = true;
                                } else {
                                    int stepNum = CurrentC2.value.getStep();
                                    CurrentC2.value.setStep(stepNum += 1);
                                    if (CurrentC2.value.getStep() == 1) {
                                        System.out.println("Se completa la impresión de una imagen en blanco y negro y se le"
                                                + " entrega al cliente " + CurrentV3.value.getName());
                                        List imgListBW = CurrentV3.value.getImgBW();
                                        imgListBW.addImage(new Image(CurrentC2.value.getIdClient(), CurrentC2.value.getType(), CurrentC2.value.getStep()));
                                        CurrentV3.value.setImgBW(imgListBW);
                                        PrinterBW.removeImage();
                                        sameStepBW = true;
                                    }
                                }
                            }
                        }
                    }
                } else {
                    CurrentV3.value.setWait(false);
                }
                CurrentV3 = CurrentV3.next;
            }

            //Cliente sale de la empresa
            NodeDoubleC CurrentV4 = Waiting.first;
            while (CurrentV4 != null) {
                if (CurrentV4.value.getnumImgC() == CurrentV4.value.getImgC().size3 && CurrentV4.value.getnumImgBW() == CurrentV4.value.getImgBW().size3) {
                    if (CurrentV4.value.getExit() != true) {
                        System.out.println("El cliente " + CurrentV4.value.getName() + " ya posee todas sus imagenes impresas y sale de la empresa");
                        Attended.addC(new Cliente(CurrentV4.value.getId(), CurrentV4.value.getName(), CurrentV4.value.getnumImgC(), CurrentV4.value.getnumImgBW(), CurrentV4.value.getImgC(),
                                CurrentV4.value.getImgBW(), CurrentV4.value.getgiveImg(), CurrentV4.value.getWait(), CurrentV4.value.getExit(), (CurrentV4.value.getStepCont() - 1), CurrentV4.value.getvAttended()));
                        Waiting.remove(CurrentV4.value.getId());
                    } else {
                        CurrentV4.value.setExit(false);
                    }
                }
                CurrentV4 = CurrentV4.next;
            }

            System.out.println("\n");
            System.out.println(".................. CLIENTES HACIENDO COLA ..................");
            Clientes.printContentC();
            System.out.println("........................ VENTANILLAS .......................");
            Ventanillas.printContentV();
            Ventanillas.printContentVClients();
            System.out.println("...................... LISTA DE ESPERA .....................");
            Waiting.printContent();
            System.out.println("................ COLA DE IMPRESORA A COLOR .................");
            PrinterC.printContentImg();
            System.out.println(".................. COLA DE IMPRESORA A BW ..................");
            PrinterBW.printContentImg();
            System.out.println(".................... CLIENTES ATENDIDOS ....................");
            Attended.printContentC();
            globalStep++;
            break;
        }
    }

    public static void cGenerator() {
        String[] names = {"Alvaro", "Felipe", "Juan", "Carlos", "Alberto", "Sara", "Maria", "Jorge", "Isabel", "Fernanda", "Lucia", "Lourdes", "Vanessa", "Carol",
            "Sofia", "Andrea", "Abner", "Alejandra", "Gabriela", "Manuel", "Hugo", "Francisco", "Jaime", "Ivan", "Michelle"};
        String[] lastnames = {"Gonzalez", "Gomez", "Diaz", "Rodriguez", "Fernandez", "Lopez", "Garcia", "Romero", "Sanchez", "Muñoz", "Flores", "Rojas", "Morales",
            "Torres", "Espinoza", "Fuentes", "Soto", "Alvarez", "Castro", "Cortes", "Rivera", "Figueroa", "Campos", "Ortiz", "Guzman"};

        Random r = new Random();

        int clients = r.nextInt(4);
        if (clients != 0) {
            for (int i = 0; i < clients; i++) {
                int rNames = r.nextInt(25);
                int rLastNames = r.nextInt(25);
                int imagesC = r.nextInt(5);
                int imagesBW = r.nextInt(5);
                if (imagesC == 0 || imagesBW == 0) {
                    Clientes.addC(new Cliente((lastId + 1), (names[rNames] + " " + lastnames[rLastNames]), (imagesC + 1), (imagesBW + 1),
                            new List(), new List(), false, false, false, 0, ""));
                    lastId += 1;
                } else {
                    Clientes.addC(new Cliente((lastId + 1), (names[rNames] + " " + lastnames[rLastNames]), imagesC, imagesBW,
                            new List(), new List(), false, false, false, 0, ""));
                    lastId += 1;
                }
            }
        }
    }

    public static void liveStructures() {
        System.out.println("\n");
        System.out.println(".................. CLIENTES HACIENDO COLA ..................");
        Clientes.printContentC();
        System.out.println("........................ VENTANILLAS .......................");
        Ventanillas.printContentV();
        Ventanillas.printContentVClients();
        System.out.println("...................... LISTA DE ESPERA .....................");
        Waiting.printContent();
        System.out.println("................ COLA DE IMPRESORA A COLOR .................");
        PrinterC.printContentImg();
        System.out.println(".................. COLA DE IMPRESORA A BW ..................");
        PrinterBW.printContentImg();
        System.out.println(".................... CLIENTES ATENDIDOS ....................");
        Attended.printContentC();
        System.out.println("\n");
    }

    public static void topColor() {
        NodeC Current = Attended.first2;
        List sort = new List();
        while (Current != null) {
            if (sort.size2 <= 5) {
                sort.sortHigherC(Current.value);
            }
            Current = Current.next;
        }
        NodeC gCurrent = sort.first2;
        int cont = 1;
        while (gCurrent.next != null) {
            System.out.println(cont + ") Id: " + gCurrent.value.getId() + ", Nombre: " + gCurrent.value.getName() + " , Cantidad de imágenes a color: " + gCurrent.value.getnumImgC());
            gCurrent = gCurrent.next;
            cont += 1;
        }
    }

    public static void topBW() {
        NodeC Current = Attended.first2;
        List sort = new List();
        while (Current != null) {
            if (sort.size2 <= 5) {
                sort.sortSmallerBW(Current.value);
            }
            Current = Current.next;
        }
        NodeC gCurrent = sort.first2;
        int cont = 1;
        while (gCurrent.next != null) {
            System.out.println(cont + ") Id: " + gCurrent.value.getId() + ", Nombre: " + gCurrent.value.getName() + " , Cantidad de imágenes en BW: " + gCurrent.value.getnumImgBW());
            gCurrent = gCurrent.next;
            cont += 1;
        }
    }

    public static void moreStepsC() {
        NodeC Current = Attended.first2;
        List topClient = new List();
        while (Current != null) {
            if (topClient.first2 == null) {
                topClient.addC(Current.value);
            } else {
                if (Current.value.getStepCont() > topClient.first2.value.getStepCont()) {
                    topClient.removeC(topClient.first2.value.getId());
                    topClient.addC(Current.value);
                }
            }
            Current = Current.next;
        }
        System.out.println("Id: " + topClient.first2.value.getId() + ", Nombre: " + topClient.first2.value.getName() + " , Cantidad de pasos en el sistema: " + topClient.first2.value.getStepCont());
    }

    public static void drawImage(String text, int choose, int choose2) {
        createFile(text, choose);
        ProcessBuilder process = null;
        if (choose2 == 1) {
            process = new ProcessBuilder("dot", "-Tpng", "-o", "recepcion.png", "recepcion.dot");
        } else if (choose2 == 2) {
            process = new ProcessBuilder("dot", "-Tpng", "-o", "ventanillas.png", "ventanillas.dot");
        } else if (choose2 == 3) {
            process = new ProcessBuilder("dot", "-Tpng", "-o", "enEspera.png", "enEspera.dot");
        } else if (choose2 == 4) {
            process = new ProcessBuilder("dot", "-Tpng", "-o", "colaC.png", "colaC.dot");
        } else if (choose2 == 5) {
            process = new ProcessBuilder("dot", "-Tpng", "-o", "colaBW.png", "colaBW.dot");
        } else if (choose2 == 6) {
            process = new ProcessBuilder("dot", "-Tpng", "-o", "atendidos.png", "atendidos.dot");
        }
        process.redirectErrorStream(true);
        try {
            process.start();
        } catch (IOException ex) {
            System.out.println(ex);
        }
    }

    public static void createFile(String text, int type) {
        FileWriter f = null;
        PrintWriter textG = null;
        try {
            String cType = "";
            if (type == 1) {
                cType = "recepcion.dot";
            } else if (type == 2) {
                cType = "ventanillas.dot";
            } else if (type == 3) {
                cType = "enEspera.dot";
            } else if (type == 4) {
                cType = "colaC.dot";
            } else if (type == 5) {
                cType = "colaBW.dot";
            } else if (type == 6) {
                cType = "atendidos.dot";
            }
            f = new FileWriter(cType);
            textG = new PrintWriter(f);
            textG.write(text);
            textG.close();
            f.close();
        } catch (Exception e) {
            System.out.println(e);
        } finally {
            if (textG != null) {
                textG.close();
                try {
                    f.close();
                } catch (IOException ex) {
                }
            }
        }
    }

    public static String Live(int choose) {
        String result = "";
        result += "digraph G {\n";
        if (choose == 1) {
            result += "label=\"Cola de recepción\";\n";
            String conections = "";
            String nodes = "";
            NodeC Current = Clientes.first;
            while (Current != null) {
                nodes += Current.hashCode() + "[label=\" Id: " + Current.value.getId() + ", Nombre: " + Current.value.getName() + "\"];\n";
                if (Current.next != null) {
                    conections += Current.hashCode() + "->" + Current.next.hashCode() + ";\n";
                }
                Current = Current.next;
            }
            result += nodes;
            result += "{rank=same;\n";
            result += conections;
            result += "}\n";
            result += "}\n";
        } else if (choose == 2) {
            result += "label=\"Lista de Ventanillas\";\n";
            String conections = "";
            String nodes = "";
            String images = "";
            NodeV Current = Ventanillas.first;
            while (Current != null) {
                nodes += Current.hashCode() + "[label=\" Nombre: " + Current.value.getName() + ", Id Cliente: " + Current.value.getClient().value.getId() + ", Nombre: " + Current.value.getClient().value.getName() + "\"];\n";
                NodeImage ni = Current.value.getImages().first;
                while (ni != null) {
                    nodes += ni.hashCode() + "[label=\" Tipo: " + ni.value.getType() + ", Cliente al que pertenece: " + ni.value.getIdClient() + "\"];\n";
                    ni = ni.next;
                }
                NodeImage ni2 = Current.value.getImages().first;
                if (ni2 != null) {
                    images += Current.hashCode() + "->" + ni2.hashCode() + ";\n";
                    while (ni2.next != null) {
                        images += ni2.hashCode() + "->" + ni2.next.hashCode() + ";\n";
                        ni2 = ni2.next;
                    }
                }

                if (Current.next != null) {
                    conections += Current.hashCode() + "->" + Current.next.hashCode() + ";\n";
                }
                Current = Current.next;
            }
            result += nodes;
            result += "{rank=same;\n";
            result += conections;
            result += "}\n";
            result += "{";
            result += images;
            result += "}";
            result += "}\n";
        } else if (choose == 3) {
            result += "label=\"Lista de Clientes en espera\";\n";
            String conections = "";
            String nodes = "";
            NodeDoubleC Current = Waiting.first;
            while (Current != null) {
                nodes += Current.hashCode() + "[label=\" Id: " + Current.value.getId() + ", Nombre: " + Current.value.getName() + ", Img a color actualmente: " + Current.value.getImgC().size3 + ", Img en BW actualmente: " + Current.value.getImgBW().size3 + "\"];\n";
                if (Current.next != null) {
                    conections += Current.hashCode() + "->" + Current.next.hashCode() + ";\n";
                }
                Current = Current.next;
            }

            result += nodes;
            result += "{rank=same;\n";
            result += conections;
            result += "}\n";
            result += "}\n";
        } else if (choose == 4) {
            result += "label=\"Cola de impresión a Color\";\n";
            String conections = "";
            String nodes = "";
            NodeImage Current = PrinterC.first2;
            while (Current != null) {
                nodes += Current.hashCode() + "[label=\" Id del cliente: " + Current.value.getIdClient() + ", Pasos que lleva: " + Current.value.getStep() + "\"];\n";
                if (Current.next != null) {
                    conections += Current.hashCode() + "->" + Current.next.hashCode() + ";\n";
                }
                Current = Current.next;
            }
            result += nodes;
            result += "{rank=same;\n";
            result += conections;
            result += "}\n";
            result += "}\n";
        } else if (choose == 5) {
            result += "label=\"Cola de impresión en Blanco y negro\";\n";
            String conections = "";
            String nodes = "";
            NodeImage Current = PrinterBW.first2;
            while (Current != null) {
                nodes += Current.hashCode() + "[label=\" Id del cliente: " + Current.value.getIdClient() + ", Pasos que lleva: " + Current.value.getStep() + "\"];\n";
                if (Current.next != null) {
                    conections += Current.hashCode() + "->" + Current.next.hashCode() + ";\n";
                }
                Current = Current.next;
            }
            result += nodes;
            result += "{rank=same;\n";
            result += conections;
            result += "}\n";
            result += "}\n";
        } else if (choose == 6) {
            result += "label=\"Lista de clientes atendidos\";\n";
            String conections = "";
            String nodes = "";
            NodeC Current = Attended.first2;
            while (Current != null) {
                nodes += Current.hashCode() + "[label=\" Id: " + Current.value.getId() + ", Nombre: " + Current.value.getName() + "\"];\n";
                if (Current.next != null) {
                    conections += Current.hashCode() + "->" + Current.next.hashCode() + ";\n";
                }
                Current = Current.next;
            }
            result += nodes;
            result += "{rank=same;\n";
            result += conections;
            result += "}\n";
            result += "}\n";
        }
        return result;
    }

    public static void studentData() {
        System.out.println("\n");
        System.out.println("...................... DATOS ESTUDIANTE ....................");
        System.out.println("               Estructuras de datos Sección B");
        System.out.println("                Marvin Alexis Estrada Florian");
        System.out.println("                         201800476");
    }

    public static void menu() {
        while (play) {
            System.out.println("\n");
            System.out.println("*******************************************");
            System.out.println("*             MENÚ PRINCIPAL              *");
            System.out.println("*******************************************");
            System.out.println("* 1) Parametros iniciales                 *");
            System.out.println("* 2) Ejecutar paso                        *");
            System.out.println("* 3) Estado en memoria de las estructuras *");
            System.out.println("* 4) Reportes                             *");
            System.out.println("* 5) Acerca de                            *");
            System.out.println("* 6) Salir                                *");
            System.out.println("*******************************************");
            System.out.print("Elige una opcion: ");
            Scanner s = new Scanner(System.in);
            int option = s.nextInt();
            switch (option) {
                case 1: {
                    System.out.println("\n");
                    System.out.println("*******************************************");
                    System.out.println("*          PARAMETROS INICIALES           *");
                    System.out.println("*******************************************");
                    System.out.println("* 1) Carga masiva                         *");
                    System.out.println("* 2) Cantidad de ventanillas              *");
                    System.out.println("* 3) Regresar                             *");
                    System.out.println("*******************************************");
                    System.out.print("Elige una opcion: ");
                    Scanner s2 = new Scanner(System.in);
                    int option2 = s2.nextInt();
                    switch (option2) {
                        case 1:
                            loadJson();
                            break;
                        case 2:
                            vGenerator();
                            break;
                        case 3:
                            break;
                        default:
                            System.out.println("\n");
                            System.out.println("Advertencia: Opción no valida");
                            break;
                    }
                    break;
                }
                case 2:
                    simulation();
                    break;
                case 3: {
                    System.out.println("\n");
                    System.out.println("**********************************************");
                    System.out.println("*          ESTADO DE LAS ESTRUCTURAS         *");
                    System.out.println("**********************************************");
                    System.out.println("* 1) Cola de recepción                       *");
                    System.out.println("* 2) Lista de ventanillas                    *");
                    System.out.println("* 3) Lista de clientes en espera             *");
                    System.out.println("* 4) Cola de impresión                       *");
                    System.out.println("* 5) Lista de clientes atendidos             *");
                    System.out.println("* 6) Regresar                                *");
                    System.out.println("**********************************************");
                    System.out.print("Elige una opcion: ");
                    Scanner s2 = new Scanner(System.in);
                    int option2 = s2.nextInt();
                    switch (option2) {
                        case 1:
                            try {
                                drawImage(Live(1), 1, 1);
                                System.out.println("\n");
                                System.out.println("Información: Reporte generado");
                            } catch (Exception e) {
                                System.out.println("\n");
                                System.out.println("Advertencia: El reporte no fué generado, intente de nuevo");
                            }
                            break;
                        case 2:
                            try {
                                drawImage(Live(2), 2, 2);
                                System.out.println("\n");
                                System.out.println("Información: Reporte generado");
                            } catch (Exception e) {
                                System.out.println("\n");
                                System.out.println("Advertencia: El reporte no fué generado, intente de nuevo");
                            }
                            break;
                        case 3:
                            try {
                                drawImage(Live(3), 3, 3);
                                System.out.println("\n");
                                System.out.println("Información: Reporte generado");
                            } catch (Exception e) {
                                System.out.println("\n");
                                System.out.println("Advertencia: El reporte no fué generado, intente de nuevo");
                            }
                            break;
                        case 4:
                            try {
                                drawImage(Live(4), 4, 4);
                                drawImage(Live(5), 5, 5);
                                System.out.println("\n");
                                System.out.println("Información: Reportes generados");
                            } catch (Exception e) {
                                System.out.println("\n");
                                System.out.println("Advertencia: Los reportes no fueron generados, intente de nuevo");
                            }
                            break;
                        case 5:
                            try {
                                drawImage(Live(6), 6, 6);
                                System.out.println("\n");
                                System.out.println("Información: Reporte generado");
                            } catch (Exception e) {
                                System.out.println("\n");
                                System.out.println("Advertencia: El reporte no fué generado, intente de nuevo");
                            }
                            break;
                        case 6:
                            break;
                        default:
                            System.out.println("\n");
                            System.out.println("Advertencia: Opción no valida");
                            break;
                    }
                    break;
                }
                case 4:
                    System.out.println("\n");
                    System.out.println("**********************************************");
                    System.out.println("*                   REPORTES                 *");
                    System.out.println("**********************************************");
                    System.out.println("* 1) Top 5 clientes con mas imágenes a color *");
                    System.out.println("* 2) Top 5 clientes con mas imágenes a BW    *");
                    System.out.println("* 3) Cliente con más pasos en el sistema     *");
                    System.out.println("* 4) Información de un cliente específico    *");
                    System.out.println("* 5) Regresar                                *");
                    System.out.println("**********************************************");
                    System.out.print("Elige una opcion: ");
                    Scanner s3 = new Scanner(System.in);
                    int option3 = s3.nextInt();
                    switch (option3) {
                        case 1:
                            System.out.println("\n");
                            System.out.println("........... TOP 5 CLIENTES CON MÁS IMÁGENES A COLOR ...........");
                            topColor();
                            break;
                        case 2:
                            System.out.println("\n");
                            System.out.println("........... TOP 5 CLIENTES CON MENOS IMÁGENES EN BW ...........");
                            topBW();
                            break;
                        case 3:
                            System.out.println("\n");
                            System.out.println("............. CLIENTE CON MÁS PASOS EN EL SISTEMA .............");
                            moreStepsC();
                            break;
                        case 4:
                            System.out.println("\n");
                            System.out.println("................ DATOS DE UN CLIENTE ESPECÍFICO ...............");
                            System.out.print("Ingrese el id del cliente a buscar: ");
                            Scanner id = new Scanner(System.in);
                            Attended.SearchClient(id.nextInt());
                            System.out.println("\n");
                            break;
                        case 5:
                            break;
                        default:
                            System.out.println("\n");
                            System.out.println("Advertencia: Opción no valida");
                            break;
                    }
                    break;
                case 5:
                    studentData();
                    break;
                case 6:
                    System.out.println("\n");
                    System.out.println("Has salido del programa");
                    System.out.println("\n");
                    play = false;
                    break;
                default:
                    System.out.println("\n");
                    System.out.println("Advertencia: Opción no valida");
                    break;
            }
        }
    }
}
