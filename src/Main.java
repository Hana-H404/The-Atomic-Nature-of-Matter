import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Main extends JFrame implements ActionListener {

    private JTextField pathField;
    private JTextField deltaField;
    private JTextField minField;
    private JButton btn;
    private JTextArea area;

    public Main() {
        setTitle("Main");
        setSize(480, 460);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel top = new JPanel(new GridLayout(4, 2, 5, 5));

        top.add(new JLabel(" path:"));
        pathField = new JTextField();
        top.add(pathField);

        top.add(new JLabel(" delta:"));
        deltaField = new JTextField();
        top.add(deltaField);

        top.add(new JLabel(" minPixel:"));
        minField = new JTextField();
        top.add(minField);

        btn = new JButton("start");
        btn.addActionListener(this);
        top.add(btn);
        top.add(new JLabel(""));

        add(top, BorderLayout.NORTH);

        area = new JTextArea();
        area.setEditable(false);
        add(new JScrollPane(area), BorderLayout.CENTER);

        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String path = pathField.getText().trim();
        String d = deltaField.getText().trim();
        String m = minField.getText().trim();

        if (path.equals("") || d.equals("") || m.equals("")) {
            JOptionPane.showMessageDialog(this, "fill all the inputs!!");
            return;
        }

        double delta;
        int minPixel;
        try {
            delta = Double.parseDouble(d);
            minPixel = Integer.parseInt(m);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "wrong number!!");
            return;
        }

        btn.setEnabled(false);
        area.setText("wait...\n");

        new Thread() {
            public void run() {
                try {
                    startProcess(path, delta, minPixel);
                    btn.setEnabled(true);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(null, ex.getMessage());
                    btn.setEnabled(true);
                }
            }
        }.start();
    }

    private void startProcess(String path, double delta, int minPixel) throws IOException {
        IDatasetGenerator gen = new DatasetGenerator(Paths.get(path));
        List<String> runs = gen.generateRunDirectories();

        IBeadTracker tracker = BeadFactory.createBeadTracker(delta, 180.0, minPixel);
        ArrayList<String> all = new ArrayList<String>();

        for (int i = 0; i < runs.size(); i++) {
            int n = countJpg(runs.get(i));
            if (n < 2)
                throw new IOException("not enough frames in " + runs.get(i));

            List<String> frames = gen.generateFramePaths(runs.get(i), n);
            String[] arr = new String[frames.size()];
            for (int j = 0; j < frames.size(); j++)
                arr[j] = frames.get(j);

            tracker.trackFrames(arr);

            BufferedReader br = new BufferedReader(new FileReader("bead_displacements.txt"));
            String line = br.readLine();
            while (line != null) {
                if (!line.trim().equals(""))
                    all.add(line);
                line = br.readLine();
            }
            br.close();

            String runName = "run_" + (i + 1);
            System.out.println(runName);

            IAvogadro one = BeadFactory.createAvogadro(0.5);
            String text = one.showAvogadro();
            area.append(runName + "\n" + text + "\n");
        }

        FileWriter fw = new FileWriter("bead_displacements.txt");
        for (int i = 0; i < all.size(); i++) {
            fw.write(all.get(i) + "\n");
        }
        fw.close();

        System.out.println("final");
        IAvogadro avo = BeadFactory.createAvogadro(0.5);
        String fin = avo.showAvogadro();
        area.append("final\n" + fin + "\n");
    }

    private int countJpg(String dirPath) {
        File dir = new File(dirPath);
        File[] files = dir.listFiles();
        if (files == null)
            return 0;

        int c = 0;
        for (int i = 0; i < files.length; i++) {
            if (files[i].isFile() && files[i].getName().endsWith(".jpg"))
                c++;
        }
        return c;
    }

    public static void main(String[] args) {
        new Main();
    }
}
