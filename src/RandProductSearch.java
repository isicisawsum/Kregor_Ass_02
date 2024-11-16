import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.io.*;

public class RandProductSearch extends JFrame {
    JPanel mainPnl;
    JPanel title;
    JPanel middle;
    JPanel bottom;
    JButton search;
    JButton add;
    JButton quit;
    JTextField name;
    JTextField description;
    JTextField IDField;
    JTextField cost;
    ArrayList<Product> products = new ArrayList<>();
    private static long recCounter = 0;
    JLabel rec;

    public RandProductSearch() {

        mainPnl = new JPanel();
        mainPnl.setLayout(new BorderLayout());

        createTop();
        mainPnl.add(title, BorderLayout.NORTH);

        createMiddle();
        mainPnl.add(middle, BorderLayout.CENTER);

        createBottom();
        mainPnl.add(bottom, BorderLayout.SOUTH);

        //game();

        add(mainPnl);
        setSize(550, 750);
        setLocation(0, 0);
        setTitle("RandProductMaker");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    private void createTop(){
        title = new JPanel();

        JLabel top = new JLabel("RandProductMaker");
        top.setFont(new Font("Impact", Font.PLAIN, 36));

        title.add(top);
    }

    private void createMiddle(){
        middle = new JPanel();
        middle.setLayout(new BoxLayout(middle, BoxLayout.Y_AXIS));

        JLabel nameLabel = new JLabel("Name:");
        JLabel descriptionLabel = new JLabel("Description:");
        JLabel IDLabel = new JLabel("ID:");
        JLabel costLabel = new JLabel("Cost:");
        rec = new JLabel("Records: 0");

        name = new JTextField();
        description = new JTextField();
        IDField = new JTextField();
        cost = new JTextField();

        name.setBorder(new LineBorder(Color.BLACK, 3));
        description.setBorder(new LineBorder(Color.BLACK, 3));
        IDField.setBorder(new LineBorder(Color.BLACK, 3));
        cost.setBorder(new LineBorder(Color.BLACK, 3));


        middle.add(nameLabel);
        middle.add(name);
        middle.add(descriptionLabel);
        middle.add(description);
        middle.add(IDLabel);
        middle.add(IDField);
        middle.add(costLabel);
        middle.add(cost);
        middle.add(rec);
    }

    private void createBottom(){
        bottom = new JPanel();

        bottom.setLayout(new GridLayout(1,2));
        add = new JButton("ADD");
        quit = new JButton("QUIT");
        search = new JButton("SEARCH");

        quit.addActionListener((ActionEvent e) -> System.exit(0));
        add.addActionListener((ActionEvent e) -> addProduct());

        bottom.add(add);
        bottom.add(search);
        bottom.add(quit);
    }

    private void addProduct(){

        if(!name.getText().isEmpty() && name.getText().length() <= 35 &&
                !description.getText().isEmpty() && description.getText().length() <= 75 &&
                !IDField.getText().isEmpty() && IDField.getText().length() <= 6 &&
                !cost.getText().isEmpty() && cost.getText().length() <= 8 && isCostValid()){

            //String productCost = cost.getText().toString(); //this is some script to get a double from the JTextField

            //accesses user's files
            File workingDirectory = new File(System.getProperty("user.dir"));
            Path path = Paths.get(workingDirectory.getPath() + "\\src\\ProductDataSearcher.bin");

            String nametoString = String.format("%35s",name.getText());
            String descriptionToString = String.format("%75s", description.getText());
            String IDToString = String.format("%6s",IDField.getText());
            String costToString = String.format("%8s",cost.getText());



            Product product = new Product(nametoString, descriptionToString, IDToString, costToString);
            products.add(product);

            saveProductData(recCounter, name.getText(), description.getText(), IDField.getText(), cost.getText(), path.toFile()); //turning to binary file

            //System.out.println(products);
            recCounter += 1;
            rec.setText("Records: " + recCounter);
            search.addActionListener((ActionEvent e) -> searchForProduct(path.toFile()));
        }
        else{
            JOptionPane.showMessageDialog(this, "Enter in all of the fields and make sure you haven't over-typed!");
        }
    }

    private boolean isCostValid() { //tests for if is a double
        try {
            Double.parseDouble(cost.getText());
            return true;
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid number for the cost.");
            return false;
        }
    }

    private static void saveProductData(long pos, String name, String desc, String ID, String cost, File file) { //code from lecture
        try (RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
            //raf.setLength(0);

            pos = pos * 124;  // Assuming each product takes up 124 bytes
            raf.seek(pos);
            String formattedName = String.format("%-35s", name);
            raf.write(formattedName.getBytes(StandardCharsets.UTF_8));

            String formattedDesc = String.format("%-75s", desc);
            raf.write(formattedDesc.getBytes(StandardCharsets.UTF_8));

            String formattedID = String.format("%-6s", ID);
            raf.write(formattedID.getBytes(StandardCharsets.UTF_8));

            String formattedCost = String.format("%-8s", cost);
            raf.write(formattedCost.getBytes(StandardCharsets.UTF_8));



            System.out.println("Data written successfully to file at position: " + pos);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void searchForProduct(File file) {
        String searchRes = JOptionPane.showInputDialog("Enter the keyword to search:");

        System.out.println("Searching for: " + searchRes);

        if (searchRes == null || searchRes.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter a keyword.");
            return;
        }

        StringBuilder res = new StringBuilder();

        /*if (!file.exists()) {
            JOptionPane.showMessageDialog(this, "Data file not found!");
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }*/

        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            long recordSize = 124; // 35 + 75 + 6 + 8 = 124 bytes per record
            long totalRecords = raf.length() / recordSize;
            boolean found = false;

            for (int i = 0; i < totalRecords; i++) {
                raf.seek(i * recordSize);
                System.out.println("loop ran");

                byte[] nameBytes = new byte[35];
                raf.read(nameBytes);
                String name = new String(nameBytes, StandardCharsets.UTF_8).trim();

                byte[] descBytes = new byte[75];
                raf.read(descBytes);
                String description = new String(descBytes, StandardCharsets.UTF_8).trim();

                byte[] idBytes = new byte[6];
                raf.read(idBytes);
                String ID = new String(idBytes, StandardCharsets.UTF_8).trim();

                byte[] costBytes = new byte[8];
                raf.read(costBytes);
                String cost = new String(costBytes, StandardCharsets.UTF_8).trim();

                System.out.println("Read Record: " + name + ", " + description + ", " + ID + ", " + cost);
                System.out.println(idBytes.toString() + nameBytes.toString() + descBytes.toString() + costBytes.toString());

                if (name.toLowerCase().contains(searchRes.toLowerCase()) ||
                        description.toLowerCase().contains(searchRes.toLowerCase()) ||
                        ID.toLowerCase().contains(searchRes.toLowerCase()) ||
                        cost.toLowerCase().contains(searchRes.toLowerCase())) {
                    found = true;
                    res.append(String.format(
                            "Record %d:%n Name: %s%n Description: %s%n ID: %s%n Cost: %s%n%n",
                            i + 1, name, description, ID, cost
                    ));
                }
            }

            if (!found) {
                res.append("No matches found for the keyword: ").append(searchRes);
            }

            JOptionPane.showMessageDialog(this, res.toString());

        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error reading the file.");
        }
    }
}
