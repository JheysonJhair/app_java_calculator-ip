package appdetailsip;

import java.awt.event.KeyEvent;
import java.util.regex.Pattern;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import java.net.InetAddress;
import java.net.UnknownHostException;
import javax.swing.table.DefaultTableModel;

/**-
 *
 * @author Jhair
 */
public class Interfaz extends javax.swing.JFrame {

/**
     * Creates new form Interfaz
     */
    public Interfaz() {
        initComponents();
        setLocationRelativeTo(null);
        setResizable(false);
        LimpiarDetalle();
        LimpiarIp();
        txtCampo1.requestFocus();
    }
    String ipRed;
    public void setDato(String user){
        lblUser.setText(user.toUpperCase());
    }
    
    //FUNCION PARA VALIDAR LA DIREECCION IP
    public static boolean validarDireccionIP(String ip) {
        String pattern = "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                          "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                          "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                          "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";       
        return Pattern.matches(pattern, ip);
    }
    public void ValidarDireccionIP() {

        String campo1 = txtCampo1.getText();
        String campo2 = txtCampo2.getText();
        String campo3 = txtCampo3.getText();
        String campo4 = txtCampo4.getText();
        
        
        String ip = campo1 + "." + campo2 + "." + campo3 + "." + campo4;

        if (validarDireccionIP(ip)) {
            verificarMascaraRed(ip, lblMascara, lblClase, lblIdentificadorRed, lblNumeroRedes);
            obtenerIdentificadorHost(ip,lblMascara, lblIdentificadorHost, lblIpRed, lblIpHost, lblBroadcast);
            obtenerNumeroIPs(ip,lblCantidadIp,lblCantidadIpConfig);
            lblIpHost.setText(txtCampo1.getText()+"."+txtCampo2.getText()+"."+txtCampo3.getText()+"."+txtCampo4.getText());
            
            calculateSubnet(ip, Integer.parseInt(txtSubRed.getText()));
        }else {
            JOptionPane.showMessageDialog(null, "Dirección IP incorrecta", "Error", JOptionPane.ERROR_MESSAGE);
            txtCampo1.requestFocus();
            LimpiarDetalle();
            LimpiarIp();
        }
    }
    
    //----------------------------------------------------------------SUB REDES
    
    private  void calculateSubnet(String ipAddress, int subnetCount) {
        try {
            InetAddress networkAddress = InetAddress.getByName(ipAddress);
            String networkClass = getNetworkClass(networkAddress);
            int prefixLength = getPrefixLength(networkClass);
            int subnetBits = getSubnetBits(subnetCount);
            int newPrefixLength = prefixLength + subnetBits;
            int networkSize = calculateNetworkSize(newPrefixLength);           
            String subnetMask = calculateSubnetMask(newPrefixLength);
            
            int lengh = obtenerValorDistinto(subnetMask);
            int jumpSize = calculateJumpSize(subnetCount);
            String ipr = ipRed;
            
            //Llenando datos de subNet
            
            lblMascaraSr.setText(""+subnetMask);
            lblnroips.setText(String.valueOf(networkSize));
            lblSalto.setText(String.valueOf(jumpSize));
            
            //Llenando la tabla

            DefaultTableModel model = new DefaultTableModel();
            model.addColumn("NRO");            
            model.addColumn("SUB RED");
            model.addColumn("IPS CONFIGURABLES");
            model.addColumn("BROADCAST");
            // Obtener la columna específica por índice
            int cont = 0;
            String[] parts = ipr.split("\\.");
            String p1,p2,p3;
            if(null != networkClass)switch (networkClass) {
                case "Clase A":
                    p1 = parts[0];  
                    for(int i=0;i<=lengh;i=i+jumpSize){
                        model.addRow(new Object[]{cont, p1+"."+i+".0.0", p1+"."+i+".0.1 - "+p1+"."+(i+(jumpSize-1))+".255.254", p1+"."+(i+(jumpSize-1))+".255.255"});
                        cont ++;
                    }   break;
                case "Clase B":
                    p1 = parts[0];  
                    p2  = parts[1];  
                    for(int i=0;i<=lengh;i=i+jumpSize){
                        model.addRow(new Object[]{cont, p1+"."+p2+"."+i+".0",  p1+".0."+i+".1 - "+p1+"."+p2+"."+(i+(jumpSize-1))+".254",   p1+"."+p2+"."+(i+(jumpSize-1))+".255"});
                        cont ++;
                    }   break;
                case "Clase C":
                    p1 = parts[0];  
                    p2  = parts[1];  
                    p3 = parts[2];
                    for(int i=0;i<=lengh;i=i+jumpSize){
                        model.addRow(new Object[]{cont,p1+"."+p2+"."+p3+"."+i,   p1+"."+p2+"."+p3+"."+(i+1)+" - "+p1+"."+p2+"."+p3+"."+(i+(jumpSize-2)),   p1+"."+p2+"."+p3+"."+(i+(jumpSize-1))});
                        cont ++;
                    }   break;
                default:
                    break;
            }          
            // Asignar el modelo a la tabla existente
            tabla.setModel(model);
            
        } catch (UnknownHostException e) {
        }
    }
    private static String getNetworkClass(InetAddress networkAddress) {
        byte[] addressBytes = networkAddress.getAddress();
        int firstByte = addressBytes[0] & 0xFF;

        if (firstByte >= 1 && firstByte <= 126) {
            return "Clase A";
        } else if (firstByte >= 128 && firstByte <= 191) {
            return "Clase B";
        } else if (firstByte >= 192 && firstByte <= 223) {
            return "Clase C";
        } else {
            return "No se puede determinar la clase de red";
        }
    }

    private static int getPrefixLength(String networkClass) {
        switch (networkClass) {
            case "Clase A":
                return 8;
            case "Clase B":
                return 16;
            case "Clase C":
                return 24;
            default:
                return 0;
        }
    }

    private static int getSubnetBits(int subnetCount) {
        int subnetBits = 0;
        int temp = subnetCount + 2; // Consideramos la dirección de red y la dirección de broadcast
        while (Math.pow(2, subnetBits) < temp) {
            subnetBits++;
        }
        return subnetBits;
    }

    private static String calculateSubnetMask(int prefixLength) {
        StringBuilder subnetMaskBuilder = new StringBuilder();
        for (int i = 0; i < 32; i++) {
            if (i < prefixLength) {
                subnetMaskBuilder.append('1');
            } else {
                subnetMaskBuilder.append('0');
            }
        }
        return convertBinaryToDecimal(subnetMaskBuilder.toString());
    }

    private static int calculateNetworkSize(int prefixLength) {
        return (int) Math.pow(2, 32 - prefixLength);
    }

    private static int calculateJumpSize(int subnetCount) {
        int jumpSize = 0;
        double salto = 0;
        
        int temp = subnetCount + 2; // Consideramos la dirección de red y la dirección de broadcast
        while (Math.pow(2, jumpSize) < temp) {
            jumpSize++;
        }
        salto = 256/ (Math.pow(2, jumpSize));
        return (int) salto;       
    }

    private static String convertBinaryToDecimal(String binaryString) {
        StringBuilder decimalBuilder = new StringBuilder();
        int decimal = 0;
        for (int i = 0; i < binaryString.length(); i++) {
            decimal = decimal * 2 + (binaryString.charAt(i) - '0');
            if ((i + 1) % 8 == 0) {
                decimalBuilder.append(decimal);
                if (i != binaryString.length() - 1) {
                    decimalBuilder.append('.');
                }
                decimal = 0;
            }
        }
        return decimalBuilder.toString();
    }
    public static int obtenerValorDistinto(String mascaraIP) {
        String[] partes = mascaraIP.split("\\.");
        for (String parte : partes) {
            int valor = Integer.parseInt(parte);
            if (valor != 0 && valor != 255) {
                return valor;
            }
        }
        return -1; // Valor de retorno para indicar que no se encontró un valor distinto de 0 o 255
    }
    //----------------------------------------------------------------------------------------------
    //FUNCION PARA VERIFICAR LA MASCARA DE RED - CLASE - IDENTIFICADOR DE RED -NUMERO DE REDES
    public void verificarMascaraRed(String direccionIP, JLabel lblMascaraRed, JLabel lblClase,  JLabel lblIdentificadorRed,JLabel lblNumeroRedes) {
        String[] octetos = direccionIP.split("\\.");
           
        int numeroRedes = 0;
        int primerOcteto = Integer.parseInt(octetos[0]);
        int segundoOcteto = Integer.parseInt(octetos[1]);
        int tercerOcteto = Integer.parseInt(octetos[2]);
        
        // Determinar la clase de la dirección IP
        char claseIP;
        if (primerOcteto >= 1 && primerOcteto <= 126) {
            claseIP = 'A';
            lblClase.setText("Clase A.");
            numeroRedes = 127;
        } else if (primerOcteto >= 128 && primerOcteto <= 191) {
            claseIP = 'B';
            lblClase.setText("Clase B.");
            numeroRedes = 16384;
        } else if (primerOcteto >= 192 && primerOcteto <= 223) {
            claseIP = 'C';
            lblClase.setText("Clase C.");
            numeroRedes = 2097152;
        } else if (primerOcteto >= 224 && primerOcteto <= 239) {
            claseIP = 'D';
            lblClase.setText("Clase D.");
        } else {
            LimpiarDetalle();
            JOptionPane.showMessageDialog(null, "Estamos frente a una dirección IP de loopback");
            System.exit(0);
            return;     
        }

        // Determinar el número de bits utilizados para la parte de red de la dirección IP
        int bitsRed;
        switch (claseIP) {
            case 'A':
                bitsRed = 8;
                break;
            case 'B':
                bitsRed = 16;
                break;
            case 'C':
                bitsRed = 24;
                break;
            default:
                bitsRed = 32;
                break;
        }

        // Construir la máscara de red en su forma decimal puntada
        int[] octetosIdentificadorRed = new int[4];
        int[] octetosMascara = new int[4];
        switch (bitsRed) {
            case 8:
                octetosMascara[0] = 255;
                octetosIdentificadorRed[0] = primerOcteto;
                break;
            case 16:
                octetosMascara[0] = 255;
                octetosMascara[1] = 255;
                octetosIdentificadorRed[0] = primerOcteto;
                octetosIdentificadorRed[1] = segundoOcteto;
                break;
            case 24:
                octetosMascara[0] = 255;
                octetosMascara[1] = 255;
                octetosMascara[2] = 255;
                octetosIdentificadorRed[0] = primerOcteto;
                octetosIdentificadorRed[1] = segundoOcteto;
                octetosIdentificadorRed[2] = tercerOcteto;
                break;
            default:
                octetosMascara[0] = 255;
                octetosMascara[1] = 255;
                octetosMascara[2] = 255;
                octetosMascara[3] = 255;
                octetosIdentificadorRed[0] = primerOcteto;
                octetosIdentificadorRed[1] = segundoOcteto;
                octetosIdentificadorRed[2] = tercerOcteto;
                break;
        }
        for (int i = 0; i < 4; i++) {
            octetosIdentificadorRed[i] = octetosIdentificadorRed[i] & octetosMascara[i];
        }
        // Establecer el identificador de red
        lblIdentificadorRed.setText(octetosIdentificadorRed[0] + "." + octetosIdentificadorRed[1] + "." + octetosIdentificadorRed[2] + "." + octetosIdentificadorRed[3]);
        txtclasesb.setText(octetosIdentificadorRed[0] + "." + octetosIdentificadorRed[1] + "." + octetosIdentificadorRed[2] + "." + octetosIdentificadorRed[3]);
        ipRed = octetosIdentificadorRed[0] + "." + octetosIdentificadorRed[1] + "." + octetosIdentificadorRed[2] + "." + octetosIdentificadorRed[3];
        // Establecer la mascara de red
        lblMascaraRed.setText( octetosMascara[0] + "." + octetosMascara[1] + "." + octetosMascara[2] + "." + octetosMascara[3]);
        
        // Establecer el numero de redes
        lblNumeroRedes.setText(Integer.toString(numeroRedes));
        
    }

    //FUNCION PARA OBTENER IDENTIFICADOR DE HOST - IP DE RED - IP DE HOST
    public static void obtenerIdentificadorHost(String direccionIP, JLabel lblMascaraRed, JLabel lblIdentificadorHost, JLabel lblIpRed, JLabel lblIpHost,JLabel lblBroadcast ) {
        String[] octetosIP = direccionIP.split("\\.");
        String mascaraRed = lblMascaraRed.getText();
        String[] octetosMascara = mascaraRed.split("\\.");

        // Convertir los octetos de la dirección IP y la máscara de red a su forma binaria
        String direccionIPBinaria = "";
        String mascaraRedBinaria = "";
        for (int i = 0; i < 4; i++) {
            direccionIPBinaria += String.format("%8s", Integer.toBinaryString(Integer.parseInt(octetosIP[i]))).replace(' ', '0');
            mascaraRedBinaria += String.format("%8s", Integer.toBinaryString(Integer.parseInt(octetosMascara[i]))).replace(' ', '0');
        }

        // Obtener la negación de la máscara de red (complemento a uno de la máscara de red)
        String mascaraRedNegadaBinaria = "";
        for (int i = 0; i < 32; i++) {
            if (mascaraRedBinaria.charAt(i) == '1') {
                mascaraRedNegadaBinaria += "0";
            } else {
                mascaraRedNegadaBinaria += "1";
            }
        }
        // Realizar una operación "AND" bit a bit entre la dirección IP y la máscara de red para obtener la dirección IP de red
        String direccionRedBinaria = "";
        for (int i = 0; i < 32; i++) {
            if (direccionIPBinaria.charAt(i) == '1' && mascaraRedBinaria.charAt(i) == '1') {
                direccionRedBinaria += "1";
            } else {
                direccionRedBinaria += "0";
            }
        }
        // Realizar una operación "AND" bit a bit entre la dirección IP y la negación de la máscara de red para obtener el identificador de host
        String identificadorHostBinario = "";
        for (int i = 0; i < 32; i++) {
            if (direccionIPBinaria.charAt(i) == '1' && mascaraRedNegadaBinaria.charAt(i) == '1') {
                identificadorHostBinario += "1";
            } else {
                identificadorHostBinario += "0";
            }
        }

        // Calcular la máscara de subred complementada
        String mascaraSubredComplementadaBinaria = "";
        for (int i = 0; i < 32; i++) {
            if (mascaraRedBinaria.charAt(i) == '1') {
                mascaraSubredComplementadaBinaria += "0";
            } else {
                mascaraSubredComplementadaBinaria += "1";
            }
        }
        // Realizar una operación "AND" bit a bit entre la dirección IP y la máscara de subred complementada para obtener la dirección IP del host

        for (int i = 0; i < 32; i++) {
            if (direccionIPBinaria.charAt(i) == '1' && mascaraSubredComplementadaBinaria.charAt(i) == '1') {
            } else {
            }
        }

        // Convertir el identificador de host de su forma binaria a su forma decimal puntada
        String identificadorHostDecimalPuntado = "";
        String direccionRedDecimalPuntado = "";
        for (int i = 0; i < 4; i++) {
            identificadorHostDecimalPuntado += Integer.parseInt(identificadorHostBinario.substring(i * 8, (i + 1) * 8), 2);
            direccionRedDecimalPuntado += Integer.parseInt(direccionRedBinaria.substring(i * 8, (i + 1) * 8), 2);
            if (i < 3) {
                identificadorHostDecimalPuntado += ".";
                direccionRedDecimalPuntado += ".";
            }
        }
        
        // Broadcast
        // Obtener los octetos de la dirección de broadcast
        String[] octetosBroadcast = new String[4];
        for (int i = 0; i < 4; i++) {
            int octetoIP = Integer.parseInt(octetosIP[i]);
            int octetoMascara = Integer.parseInt(octetosMascara[i]);

            // Aplicar operación bitwise OR a los octetos de la dirección IP y la máscara de subred
            int octetoBroadcast = octetoIP | (~octetoMascara & 0xff);
            octetosBroadcast[i] = Integer.toString(octetoBroadcast);
        }
        
        // Unir los octetos de la dirección de broadcast en una cadena de texto
        String direccionBroadcast = String.join(".", octetosBroadcast);
        // Establecer el texto en el JLabel
        lblIpRed.setText(direccionRedDecimalPuntado);
        //lblIpHost.setText(direccionHostDecimalPuntado);
        lblBroadcast.setText(direccionBroadcast);
        lblIdentificadorHost.setText(identificadorHostDecimalPuntado);
    }
    
    //FUNCION PARA OPTENER LA CANTIDAD DE IPS
    public static long obtenerCantidadIP(JLabel lblMascaraSubred) {
        String mascaraSubred = lblMascaraSubred.getText();
        String[] octetosMascara = mascaraSubred.split("\\.");

        // Convertir los octetos de la máscara de subred a su forma binaria
        String mascaraSubredBinaria = "";
        for (int i = 0; i < 4; i++) {
            mascaraSubredBinaria += String.format("%8s", Integer.toBinaryString(Integer.parseInt(octetosMascara[i]))).replace(' ', '0');
        }

        // Contar el número de bits que están a 1 en la máscara de subred
        int numeroBitsUno = 0;
        for (int i = 0; i < 32; i++) {
            if (mascaraSubredBinaria.charAt(i) == '1') {
                numeroBitsUno++;
            }
        }

        // Calcular la cantidad de direcciones IP disponibles en la red
        long cantidadIP = (long) Math.pow(2, 32 - numeroBitsUno);

        return cantidadIP;
    }
    
    //FUNCION PARA CALCULAR LA CANTIDAD DE IP CONFIGURABLES
    public void obtenerNumeroIPs(String direccionIP,JLabel lblCantidadIp, JLabel lblCantidadIpConfig ) {
        String[] octetosIP = direccionIP.split("\\.");

        // Determinar la clase de dirección IP
        int primerOcteto = Integer.parseInt(octetosIP[0]);
        char claseIP = ' ';
        if (primerOcteto >= 1 && primerOcteto <= 126) {
            claseIP = 'A';
        } else if (primerOcteto >= 128 && primerOcteto <= 191) {
            claseIP = 'B';
        } else if (primerOcteto >= 192 && primerOcteto <= 223) {
            claseIP = 'C';
        } else if (primerOcteto >= 224 && primerOcteto <= 239) {
            claseIP = 'D';
        }
        // Calcular el número de direcciones IP configurables
        int numeroIPs = 0;
        switch (claseIP) {
            case 'A':
                numeroIPs = (int) Math.pow(2, 24);
                break;
            case 'B':
                numeroIPs = (int) Math.pow(2, 16);
                break;
            case 'C':
                numeroIPs = (int) Math.pow(2, 8);
                break;
            case 'D':
                numeroIPs = (int) Math.pow(2, 8);
                break;
                
        }

        lblCantidadIp.setText(Integer.toString(numeroIPs));
        lblCantidadIpConfig.setText(Integer.toString(numeroIPs-2));
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jSeparator1 = new javax.swing.JSeparator();
        jSeparator2 = new javax.swing.JSeparator();
        jLabel1 = new javax.swing.JLabel();
        txtCampo1 = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        txtCampo2 = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        txtCampo3 = new javax.swing.JTextField();
        txtCampo4 = new javax.swing.JTextField();
        btnDetalles = new javax.swing.JButton();
        jLabel8 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jLabel17 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        jLabel20 = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        jLabel22 = new javax.swing.JLabel();
        jLabel23 = new javax.swing.JLabel();
        jLabel24 = new javax.swing.JLabel();
        jLabel25 = new javax.swing.JLabel();
        jLabel26 = new javax.swing.JLabel();
        lblClase = new javax.swing.JLabel();
        lblMascara = new javax.swing.JLabel();
        lblIdentificadorRed = new javax.swing.JLabel();
        lblIdentificadorHost = new javax.swing.JLabel();
        lblIpRed = new javax.swing.JLabel();
        lblIpHost = new javax.swing.JLabel();
        lblCantidadIp = new javax.swing.JLabel();
        lblCantidadIpConfig = new javax.swing.JLabel();
        lblNumeroRedes = new javax.swing.JLabel();
        lblBroadcast = new javax.swing.JLabel();
        jButton2 = new javax.swing.JButton();
        jLabel9 = new javax.swing.JLabel();
        lblUser = new javax.swing.JLabel();
        txtSubRed = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tabla = new javax.swing.JTable();
        jPanel2 = new javax.swing.JPanel();
        jLabel35 = new javax.swing.JLabel();
        jLabel36 = new javax.swing.JLabel();
        txtclasesb = new javax.swing.JLabel();
        lblMascaraSr = new javax.swing.JLabel();
        jLabel37 = new javax.swing.JLabel();
        lblIpsSubRed = new javax.swing.JLabel();
        lblnroips = new javax.swing.JLabel();
        lblSalto = new javax.swing.JLabel();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenuItem2 = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jSeparator1.setForeground(new java.awt.Color(153, 153, 153));

        jSeparator2.setForeground(new java.awt.Color(153, 153, 153));

        jLabel1.setBackground(java.awt.Color.lightGray);
        jLabel1.setText("Dirección IP:");

        txtCampo1.setFont(new java.awt.Font("Yu Gothic UI Light", 1, 17)); // NOI18N
        txtCampo1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                txtCampo1KeyPressed(evt);
            }
        });

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel2.setText(".");

        jLabel6.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel6.setText(".");

        txtCampo2.setFont(new java.awt.Font("Yu Gothic UI Light", 1, 17)); // NOI18N
        txtCampo2.setText("23");
        txtCampo2.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                txtCampo2KeyPressed(evt);
            }
        });

        jLabel7.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel7.setText(".");

        txtCampo3.setFont(new java.awt.Font("Yu Gothic UI Light", 1, 17)); // NOI18N
        txtCampo3.setText("6");
        txtCampo3.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                txtCampo3KeyPressed(evt);
            }
        });

        txtCampo4.setFont(new java.awt.Font("Yu Gothic UI Light", 1, 17)); // NOI18N
        txtCampo4.setText("5");
        txtCampo4.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                txtCampo4KeyPressed(evt);
            }
        });

        btnDetalles.setBackground(java.awt.SystemColor.control);
        btnDetalles.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/IconoBuscar.png"))); // NOI18N
        btnDetalles.setText("VER DETALLES");
        btnDetalles.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnDetalles.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDetallesActionPerformed(evt);
            }
        });

        jLabel8.setFont(new java.awt.Font("Palatino Linotype", 0, 18)); // NOI18N
        jLabel8.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/ip.png"))); // NOI18N
        jLabel8.setText("CALCULADORA DE DATOS DE UNA DIRECCIÓN IP");

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED), "Detalles de la IP", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Segoe UI Semibold", 0, 15), new java.awt.Color(102, 102, 102))); // NOI18N

        jLabel17.setFont(new java.awt.Font("Yu Gothic UI Semibold", 0, 15)); // NOI18N
        jLabel17.setText("IP DE BROADCAST:");

        jLabel18.setFont(new java.awt.Font("Yu Gothic UI Semibold", 0, 15)); // NOI18N
        jLabel18.setText("NUMERO DE REDES DISTINTAS:");

        jLabel19.setFont(new java.awt.Font("Yu Gothic UI Semibold", 0, 15)); // NOI18N
        jLabel19.setText("CANTIDAD IP CONFIGURABLES:");

        jLabel20.setFont(new java.awt.Font("Yu Gothic UI Semibold", 0, 15)); // NOI18N
        jLabel20.setText("CANTIDAD IP:");

        jLabel21.setFont(new java.awt.Font("Yu Gothic UI Semibold", 0, 15)); // NOI18N
        jLabel21.setText("IP HOST:");

        jLabel22.setFont(new java.awt.Font("Yu Gothic UI Semibold", 0, 15)); // NOI18N
        jLabel22.setText("IP RED:");

        jLabel23.setFont(new java.awt.Font("Yu Gothic UI Semibold", 0, 15)); // NOI18N
        jLabel23.setText("IDENTIFICADOR DE HOST:");

        jLabel24.setFont(new java.awt.Font("Yu Gothic UI Semibold", 0, 15)); // NOI18N
        jLabel24.setText("IDENTIFICADOR DE RED:");

        jLabel25.setFont(new java.awt.Font("Yu Gothic UI Semibold", 0, 15)); // NOI18N
        jLabel25.setText("MASCARA DE RED:");

        jLabel26.setFont(new java.awt.Font("Yu Gothic UI Semibold", 0, 15)); // NOI18N
        jLabel26.setText("CLASE: ");

        lblClase.setFont(new java.awt.Font("Verdana", 0, 16)); // NOI18N
        lblClase.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblClase.setText("...");
        lblClase.setMaximumSize(new java.awt.Dimension(10, 10));
        lblClase.setMinimumSize(new java.awt.Dimension(150, 150));

        lblMascara.setFont(new java.awt.Font("Verdana", 0, 16)); // NOI18N
        lblMascara.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblMascara.setText("...");
        lblMascara.setMaximumSize(new java.awt.Dimension(10, 10));
        lblMascara.setMinimumSize(new java.awt.Dimension(150, 150));

        lblIdentificadorRed.setFont(new java.awt.Font("Verdana", 0, 16)); // NOI18N
        lblIdentificadorRed.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblIdentificadorRed.setText("...");
        lblIdentificadorRed.setMaximumSize(new java.awt.Dimension(10, 10));
        lblIdentificadorRed.setMinimumSize(new java.awt.Dimension(150, 150));

        lblIdentificadorHost.setFont(new java.awt.Font("Verdana", 0, 16)); // NOI18N
        lblIdentificadorHost.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblIdentificadorHost.setText("...");
        lblIdentificadorHost.setMaximumSize(new java.awt.Dimension(10, 10));
        lblIdentificadorHost.setMinimumSize(new java.awt.Dimension(150, 150));

        lblIpRed.setFont(new java.awt.Font("Verdana", 0, 16)); // NOI18N
        lblIpRed.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblIpRed.setText("...");
        lblIpRed.setMaximumSize(new java.awt.Dimension(10, 10));
        lblIpRed.setMinimumSize(new java.awt.Dimension(150, 150));

        lblIpHost.setFont(new java.awt.Font("Verdana", 0, 16)); // NOI18N
        lblIpHost.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblIpHost.setText("...");
        lblIpHost.setMaximumSize(new java.awt.Dimension(10, 10));
        lblIpHost.setMinimumSize(new java.awt.Dimension(150, 150));

        lblCantidadIp.setFont(new java.awt.Font("Verdana", 0, 16)); // NOI18N
        lblCantidadIp.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblCantidadIp.setText("...");
        lblCantidadIp.setMaximumSize(new java.awt.Dimension(10, 10));
        lblCantidadIp.setMinimumSize(new java.awt.Dimension(150, 150));

        lblCantidadIpConfig.setFont(new java.awt.Font("Verdana", 0, 16)); // NOI18N
        lblCantidadIpConfig.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblCantidadIpConfig.setText("...");
        lblCantidadIpConfig.setMaximumSize(new java.awt.Dimension(10, 10));
        lblCantidadIpConfig.setMinimumSize(new java.awt.Dimension(150, 150));

        lblNumeroRedes.setFont(new java.awt.Font("Verdana", 0, 16)); // NOI18N
        lblNumeroRedes.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblNumeroRedes.setText("...");
        lblNumeroRedes.setMaximumSize(new java.awt.Dimension(10, 10));
        lblNumeroRedes.setMinimumSize(new java.awt.Dimension(150, 150));

        lblBroadcast.setFont(new java.awt.Font("Verdana", 0, 16)); // NOI18N
        lblBroadcast.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblBroadcast.setText("...");
        lblBroadcast.setMaximumSize(new java.awt.Dimension(10, 10));
        lblBroadcast.setMinimumSize(new java.awt.Dimension(150, 150));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel24)
                    .addComponent(jLabel23)
                    .addComponent(jLabel22)
                    .addComponent(jLabel21)
                    .addComponent(jLabel20)
                    .addComponent(jLabel19)
                    .addComponent(jLabel18)
                    .addComponent(jLabel17))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 36, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblBroadcast, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblCantidadIp, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblIpHost, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblIpRed, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblIdentificadorHost, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblIdentificadorRed, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblMascara, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblClase, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(lblNumeroRedes, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(lblCantidadIpConfig, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(20, 20, 20))
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel1Layout.createSequentialGroup()
                    .addGap(9, 9, 9)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jLabel25)
                        .addComponent(jLabel26))
                    .addContainerGap(291, Short.MAX_VALUE)))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(lblClase, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(lblMascara, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblIdentificadorRed, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel24))
                .addGap(19, 19, 19)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblIdentificadorHost, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel23))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblIpRed, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel22))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblIpHost, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel21))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblBroadcast, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel17))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblCantidadIp, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel20))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblCantidadIpConfig, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel19))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblNumeroRedes, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel18))
                .addContainerGap(64, Short.MAX_VALUE))
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel1Layout.createSequentialGroup()
                    .addGap(7, 7, 7)
                    .addComponent(jLabel26)
                    .addGap(18, 18, 18)
                    .addComponent(jLabel25)
                    .addContainerGap(368, Short.MAX_VALUE)))
        );

        jButton2.setBackground(java.awt.SystemColor.control);
        jButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/IconoEliminar.png"))); // NOI18N
        jButton2.setText("LIMPIAR");
        jButton2.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jLabel9.setFont(new java.awt.Font("Verdana", 0, 16)); // NOI18N
        jLabel9.setForeground(new java.awt.Color(51, 51, 51));
        jLabel9.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/IconoLogin.png"))); // NOI18N

        lblUser.setText("Anonimo");

        txtSubRed.setFont(new java.awt.Font("Yu Gothic UI Light", 1, 17)); // NOI18N
        txtSubRed.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                txtSubRedKeyPressed(evt);
            }
        });

        jLabel3.setBackground(java.awt.Color.lightGray);
        jLabel3.setText("Sub Red:");

        tabla.setFont(new java.awt.Font("Tw Cen MT", 0, 16)); // NOI18N
        tabla.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        tabla.setRowHeight(30);
        jScrollPane1.setViewportView(tabla);

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED), "Detalles de la Sub Red", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Segoe UI Semibold", 0, 15), new java.awt.Color(102, 102, 102))); // NOI18N

        jLabel35.setFont(new java.awt.Font("Yu Gothic UI Semibold", 0, 15)); // NOI18N
        jLabel35.setText("MASCARA DE RED:");

        jLabel36.setFont(new java.awt.Font("Yu Gothic UI Semibold", 0, 15)); // NOI18N
        jLabel36.setText("ID SUB RED");

        txtclasesb.setFont(new java.awt.Font("Verdana", 0, 16)); // NOI18N
        txtclasesb.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        txtclasesb.setText("...");
        txtclasesb.setMaximumSize(new java.awt.Dimension(10, 10));
        txtclasesb.setMinimumSize(new java.awt.Dimension(150, 150));

        lblMascaraSr.setFont(new java.awt.Font("Verdana", 0, 16)); // NOI18N
        lblMascaraSr.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblMascaraSr.setText("...");
        lblMascaraSr.setMaximumSize(new java.awt.Dimension(10, 10));
        lblMascaraSr.setMinimumSize(new java.awt.Dimension(150, 150));

        jLabel37.setFont(new java.awt.Font("Yu Gothic UI Semibold", 0, 15)); // NOI18N
        jLabel37.setText("SALTO:");

        lblIpsSubRed.setFont(new java.awt.Font("Yu Gothic UI Semibold", 0, 15)); // NOI18N
        lblIpsSubRed.setText("NRO IPS SUB RED:");

        lblnroips.setFont(new java.awt.Font("Verdana", 0, 16)); // NOI18N
        lblnroips.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblnroips.setText("...");
        lblnroips.setMaximumSize(new java.awt.Dimension(10, 10));
        lblnroips.setMinimumSize(new java.awt.Dimension(150, 150));

        lblSalto.setFont(new java.awt.Font("Verdana", 0, 16)); // NOI18N
        lblSalto.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblSalto.setText("...");
        lblSalto.setMaximumSize(new java.awt.Dimension(10, 10));
        lblSalto.setMinimumSize(new java.awt.Dimension(150, 150));

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel35)
                    .addComponent(jLabel36))
                .addGap(39, 39, 39)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblMascaraSr, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtclasesb, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblIpsSubRed)
                    .addComponent(jLabel37))
                .addGap(38, 38, 38)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblnroips, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblSalto, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(35, 35, 35))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(txtclasesb, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(lblMascaraSr, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel36)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel35)))
                .addContainerGap(21, Short.MAX_VALUE))
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(lblSalto, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(lblnroips, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel37)
                        .addGap(18, 18, 18)
                        .addComponent(lblIpsSubRed)))
                .addGap(0, 0, Short.MAX_VALUE))
        );

        jMenu1.setText("Menu");

        jMenuItem1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/asercade.png"))); // NOI18N
        jMenuItem1.setText("Aserca de");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem1);

        jMenuItem2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/salir.png"))); // NOI18N
        jMenuItem2.setText("Salir");
        jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem2ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem2);

        jMenuBar1.add(jMenu1);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(lblUser, javax.swing.GroupLayout.PREFERRED_SIZE, 363, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jButton2))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(33, 33, 33)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jScrollPane1)
                                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                        .addGap(12, 12, 12))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jSeparator1, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jSeparator2, javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                                .addComponent(jLabel1)
                                .addGap(18, 18, 18)
                                .addComponent(txtCampo1, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtCampo2, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel6)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtCampo3, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel7)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtCampo4, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(582, 582, 582)
                                .addComponent(jLabel3)
                                .addGap(18, 18, 18)
                                .addComponent(txtSubRed, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(37, 37, 37)
                                .addComponent(btnDetalles, javax.swing.GroupLayout.PREFERRED_SIZE, 168, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(23, 23, 23))))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 509, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(370, 370, 370))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(txtCampo1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtCampo2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtCampo3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtCampo4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnDetalles)
                    .addComponent(jLabel3)
                    .addComponent(txtSubRed, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(20, 20, 20)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 358, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jButton2)
                        .addComponent(lblUser)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnDetallesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDetallesActionPerformed

        if (!"".equals(txtCampo1.getText()) || !"".equals(txtCampo2.getText()) || !"".equals(txtCampo3.getText()) || !"".equals(txtCampo4.getText())) {
            ValidarDireccionIP();
        } else {
            JOptionPane.showMessageDialog(null, "Complete los campos!");
        }
    }//GEN-LAST:event_btnDetallesActionPerformed

    private void jMenuItem2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem2ActionPerformed
        dispose();
    }//GEN-LAST:event_jMenuItem2ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        txtCampo1.requestFocus();
        LimpiarDetalle();
        LimpiarIp();
        DefaultTableModel model = (DefaultTableModel) tabla.getModel();
        model.setRowCount(0);
    }//GEN-LAST:event_jButton2ActionPerformed

    private void txtCampo1KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtCampo1KeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            txtCampo2.requestFocus();
        }
    }//GEN-LAST:event_txtCampo1KeyPressed

    private void txtCampo2KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtCampo2KeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            txtCampo3.requestFocus();
        }
    }//GEN-LAST:event_txtCampo2KeyPressed

    private void txtCampo3KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtCampo3KeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            txtCampo4.requestFocus();
        }
    }//GEN-LAST:event_txtCampo3KeyPressed

    private void txtCampo4KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtCampo4KeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            txtSubRed.requestFocus();
            //ValidarDireccionIP();
        }
    }//GEN-LAST:event_txtCampo4KeyPressed

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
        dispose();
        Informacion in = new Informacion();
        in.setVisible(true);
    }//GEN-LAST:event_jMenuItem1ActionPerformed

    private void txtSubRedKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtSubRedKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            String subRed = txtSubRed.getText();
            if(subRed.isEmpty()){
                txtSubRed.setText("1");
            }else{
                ValidarDireccionIP();
            }
        }
    }//GEN-LAST:event_txtSubRedKeyPressed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Interfaz.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Interfaz.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Interfaz.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Interfaz.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Interfaz().setVisible(true);
            }
        });
    }



    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnDetalles;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel35;
    private javax.swing.JLabel jLabel36;
    private javax.swing.JLabel jLabel37;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JLabel lblBroadcast;
    private javax.swing.JLabel lblCantidadIp;
    private javax.swing.JLabel lblCantidadIpConfig;
    private javax.swing.JLabel lblClase;
    private javax.swing.JLabel lblIdentificadorHost;
    private javax.swing.JLabel lblIdentificadorRed;
    private javax.swing.JLabel lblIpHost;
    private javax.swing.JLabel lblIpRed;
    private javax.swing.JLabel lblIpsSubRed;
    private javax.swing.JLabel lblMascara;
    private javax.swing.JLabel lblMascaraSr;
    private javax.swing.JLabel lblNumeroRedes;
    private javax.swing.JLabel lblSalto;
    private javax.swing.JLabel lblUser;
    private javax.swing.JLabel lblnroips;
    private javax.swing.JTable tabla;
    private javax.swing.JTextField txtCampo1;
    private javax.swing.JTextField txtCampo2;
    private javax.swing.JTextField txtCampo3;
    private javax.swing.JTextField txtCampo4;
    private javax.swing.JTextField txtSubRed;
    private javax.swing.JLabel txtclasesb;
    // End of variables declaration//GEN-END:variables
    
    private void LimpiarDetalle() {
        lblBroadcast.setText(" ...");
        lblCantidadIp.setText("...");
        lblCantidadIpConfig.setText("...");
        lblClase.setText("...");
        lblIdentificadorHost.setText("...");
        lblIdentificadorRed.setText("...");
        lblIpHost.setText("...");
        lblIpRed.setText("...");
        lblMascara.setText("...");
        lblNumeroRedes.setText("...");
        
        //SUB RED
        txtclasesb.setText("");
        lblMascaraSr.setText("");
        lblSalto.setText("");
        lblnroips.setText("");
    }
    private void LimpiarIp(){
        txtCampo1.setText("");
        txtCampo2.setText("");
        txtCampo3.setText("");
        txtCampo4.setText("");
        txtSubRed.setText("");
    }
}

