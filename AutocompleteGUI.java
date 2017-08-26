import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Created by Jake on 4/4/2017.

// My terrible attempt at figuring out how to use Swing.
public class AutocompleteGUI implements ActionListener, ChangeListener, DocumentListener {
    private String old_text;
    private Autocompleter autocompleter;
    private HashMap<String, String> default_corpora;

    private JFrame main_frame;
    private JTextField add_word_input;
    private JTextField look_word_input;
    private JLabel look_word_output;
    private JSpinner max_error_input;
    private JSpinner num_results_input;
    private JTextField corpus_input;
    private ButtonGroup corpus_btn_group;
    private JTextField prefix_input;
    private JList completion_output;

    public AutocompleteGUI() {
        this("datasets/words-tiny.txt");
    }

    public AutocompleteGUI(String dataset_path) {
        boolean is_path_bad = false;
        Trie trie = Trie.fromFile(dataset_path);
        if (trie == null) {
            is_path_bad = true;
            trie = Trie.fromFile("datasets/words-tiny.txt");
        }

        this.autocompleter = Autocompleter.build(trie, 0);

        this.old_text = "";

        this.default_corpora = new HashMap<>();
        this.default_corpora.put("corpus-large", "datasets/words-large.txt");
        this.default_corpora.put("corpus-medium", "datasets/words-medium.txt");
        this.default_corpora.put("corpus-small", "datasets/words-small.txt");
        this.default_corpora.put("corpus-tiny", "datasets/words-tiny.txt");
        this.default_corpora.put("corpus-test", "datasets/words-test.txt");

        if (is_path_bad) {
            initComponents("datasets/words-tiny.txt");
        }
        else {
            initComponents(dataset_path);
        }

        displayCompletions();
        handleWordLookup();

        if (is_path_bad) {
            JOptionPane.showMessageDialog(this.main_frame, "The filepath " + dataset_path + " is invalid.\nFalling back to datasets/words-tiny.txt");
        }
    }

    private void initComponents(String dataset_path) {
        JFrame mainframe = new JFrame("CS Project");
        this.main_frame = mainframe;
        mainframe.setLayout(new GridBagLayout());
        mainframe.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        JPanel contents = new JPanel();
        contents.setLayout(new GridBagLayout());


        JPanel header = new JPanel();
        JLabel headerlbl = new JLabel("Jake Scheller - CS 2336.004 End of Term Project - Option 2: Autocomplete (See README.txt for instructions)");
        header.add(headerlbl);
        header.setBorder(BorderFactory.createCompoundBorder(new MatteBorder(0, 0, 1, 0, Color.DARK_GRAY), new EmptyBorder(3, 0, 3, 0)));


        JPanel sidebar = new JPanel();
        sidebar.setLayout(new GridBagLayout());
        sidebar.setBorder(new MatteBorder(0, 0, 0, 1, Color.DARK_GRAY));

        JPanel addwordpnl = new JPanel();
        addwordpnl.setLayout(new BoxLayout(addwordpnl, BoxLayout.PAGE_AXIS));
        JLabel addwordlbl = new JLabel("Add word (press enter to add):");
        JTextField addwordinp = new JTextField(30);
        this.add_word_input = addwordinp;
        addwordinp.setActionCommand("word-add");
        addwordinp.addActionListener(this);
        addwordpnl.add(addwordlbl);
        addwordpnl.add(addwordinp);
        addwordpnl.setBorder(BorderFactory.createCompoundBorder(new MatteBorder(0, 0, 1, 0, Color.DARK_GRAY), new EmptyBorder(10, 20, 10, 10)));

        JPanel lookwordpnl = new JPanel();
        lookwordpnl.setLayout(new BoxLayout(lookwordpnl, BoxLayout.PAGE_AXIS));
        JLabel lookwordlbl = new JLabel("Lookup word (press enter to query):");
        JTextField lookwordinp = new JTextField(30);
        this.look_word_input = lookwordinp;
        lookwordinp.setActionCommand("word-lookup");
        lookwordinp.addActionListener(this);
        JLabel lookwordout = new JLabel(" ");
        this.look_word_output = lookwordout;
        lookwordpnl.add(lookwordlbl);
        lookwordpnl.add(lookwordinp);
        lookwordpnl.add(lookwordout);
        lookwordpnl.setBorder(BorderFactory.createCompoundBorder(new MatteBorder(0, 0, 1, 0, Color.DARK_GRAY), new EmptyBorder(10, 20, 10, 10)));

        JPanel maxerrpnl = new JPanel();
        maxerrpnl.setLayout(new GridLayout(0,1));
        JLabel maxerrlbl = new JLabel("Set maximum error (0-20):");
        SpinnerNumberModel maxerrmodel = new SpinnerNumberModel(0, 0, 20, 1);
        JSpinner maxerrspn = new JSpinner(maxerrmodel);
        this.max_error_input = maxerrspn;
        maxerrspn.addChangeListener(this);
        maxerrpnl.add(maxerrlbl);
        maxerrpnl.add(maxerrspn);
        maxerrpnl.setBorder(BorderFactory.createCompoundBorder(new MatteBorder(0, 0, 1, 0, Color.DARK_GRAY), new EmptyBorder(10, 20, 10, 10)));

        JPanel resnumpnl = new JPanel();
        resnumpnl.setLayout(new GridLayout(0,1));
        JLabel resnumlbl = new JLabel("Set number of results (1-50):");
        SpinnerNumberModel resnummodel = new SpinnerNumberModel(10, 1, 50, 1);
        JSpinner resnumspn = new JSpinner(resnummodel);
        this.num_results_input = resnumspn;
        resnumspn.addChangeListener(this);
        resnumpnl.add(resnumlbl);
        resnumpnl.add(resnumspn);
        resnumpnl.setBorder(BorderFactory.createCompoundBorder(new MatteBorder(0, 0, 1, 0, Color.DARK_GRAY), new EmptyBorder(10, 20, 10, 10)));

        JPanel corppnl = new JPanel();
        corppnl.setLayout(new BoxLayout(corppnl, BoxLayout.PAGE_AXIS));
        JLabel corplbl = new JLabel("Select corpus:");
        JRadioButton corplarg = new JRadioButton("Large (351,051 words)");
        JRadioButton corpmedm = new JRadioButton("Medium (233,590 words)");
        JRadioButton corpsmal = new JRadioButton("Small (109,562 words)");
        JRadioButton corptiny = new JRadioButton("Tiny (21,086 words)");
        JRadioButton corptest = new JRadioButton("Test (8 words)");
        corplarg.setActionCommand("corpus-large");
        corpmedm.setActionCommand("corpus-medium");
        corpsmal.setActionCommand("corpus-small");
        corptiny.setActionCommand("corpus-tiny");
        corptest.setActionCommand("corpus-test");
        corplarg.addActionListener(this);
        corpmedm.addActionListener(this);
        corpsmal.addActionListener(this);
        corptiny.addActionListener(this);
        corptest.addActionListener(this);
        ButtonGroup corpgrp = new ButtonGroup();
        this.corpus_btn_group = corpgrp;
        corpgrp.add(corplarg);
        corpgrp.add(corpmedm);
        corpgrp.add(corpsmal);
        corpgrp.add(corptiny);
        corpgrp.add(corptest);
        JLabel custcorplbl = new JLabel("Enter filepath of custom corpus (press enter to use):");
        custcorplbl.setBorder(new EmptyBorder(5, 0, 0, 0));
        JTextField custcorpinp = new JTextField(30);
        this.corpus_input = custcorpinp;
        custcorpinp.setActionCommand("corpus-custom");
        custcorpinp.addActionListener(this);
        corppnl.add(corplbl);
        corppnl.add(corplarg);
        corppnl.add(corpmedm);
        corppnl.add(corpsmal);
        corppnl.add(corptiny);
        corppnl.add(corptest);
        corppnl.add(custcorplbl);
        corppnl.add(custcorpinp);
        corppnl.setBorder(new EmptyBorder(10, 20, 10, 10));

        GridBagConstraints addwordcon = new GridBagConstraints();
        addwordcon.gridx = 0;
        addwordcon.gridy = 0;
        addwordcon.weightx = 1;
        addwordcon.fill = GridBagConstraints.HORIZONTAL;
        addwordcon.anchor = GridBagConstraints.FIRST_LINE_START;
        sidebar.add(addwordpnl, addwordcon);

        GridBagConstraints lookwordcon = new GridBagConstraints();
        lookwordcon.gridx = 0;
        lookwordcon.gridy = 1;
        lookwordcon.weightx = 1;
        lookwordcon.fill = GridBagConstraints.HORIZONTAL;
        lookwordcon.anchor = GridBagConstraints.FIRST_LINE_START;
        sidebar.add(lookwordpnl, lookwordcon);

        GridBagConstraints maxerrcon = new GridBagConstraints();
        maxerrcon.gridx = 0;
        maxerrcon.gridy = 2;
        maxerrcon.weightx = 1;
        maxerrcon.fill = GridBagConstraints.HORIZONTAL;
        maxerrcon.anchor = GridBagConstraints.FIRST_LINE_START;
        sidebar.add(maxerrpnl, maxerrcon);

        GridBagConstraints resnumcon = new GridBagConstraints();
        resnumcon.gridx = 0;
        resnumcon.gridy = 3;
        resnumcon.weightx = 1;
        resnumcon.fill = GridBagConstraints.HORIZONTAL;
        resnumcon.anchor = GridBagConstraints.FIRST_LINE_START;
        sidebar.add(resnumpnl, resnumcon);

        GridBagConstraints corpcon = new GridBagConstraints();
        corpcon.gridx = 0;
        corpcon.gridy = 4;
        corpcon.weightx = 1;
        corpcon.weighty = 1;
        corpcon.fill = GridBagConstraints.HORIZONTAL;
        corpcon.anchor = GridBagConstraints.FIRST_LINE_START;
        sidebar.add(corppnl, corpcon);


        JPanel complarea = new JPanel();
        complarea.setLayout(new GridBagLayout());
        complarea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel mainwordpnl = new JPanel();
        mainwordpnl.setLayout(new GridLayout(0, 1));
        JLabel mainwordlbl = new JLabel("Enter a word here:");
        JTextField mainwordinp = new JTextField(30);
        this.prefix_input = mainwordinp;
        mainwordinp.getDocument().addDocumentListener(this);
        mainwordpnl.add(mainwordlbl);
        mainwordpnl.add(mainwordinp);

        JPanel comploutpnl = new JPanel();
        comploutpnl.setLayout(new GridLayout(0, 1));
        DefaultListModel comploutmodel = new DefaultListModel();
        JList comploutlst = new JList(comploutmodel);
        this.completion_output = comploutlst;
        comploutlst.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        comploutlst.setLayoutOrientation(JList.VERTICAL);
        comploutlst.setVisibleRowCount(10);
        comploutpnl.add(comploutlst);

        GridBagConstraints mainwordcon = new GridBagConstraints();
        mainwordcon.gridx = 0;
        mainwordcon.gridy = 0;
        mainwordcon.weightx = 1;
        mainwordcon.fill = GridBagConstraints.HORIZONTAL;
        mainwordcon.anchor = GridBagConstraints.FIRST_LINE_START;
        complarea.add(mainwordpnl, mainwordcon);

        GridBagConstraints comploutcon = new GridBagConstraints();
        comploutcon.gridx = 0;
        comploutcon.gridy = 1;
        comploutcon.weightx = 1;
        comploutcon.weighty = 1;
        comploutcon.fill = GridBagConstraints.BOTH;
        comploutcon.anchor = GridBagConstraints.FIRST_LINE_START;
        complarea.add(comploutpnl, comploutcon);


        GridBagConstraints headercon = new GridBagConstraints();
        headercon.gridx = 0;
        headercon.gridy = 0;
        headercon.gridwidth = 2;
        headercon.weightx = 1;
        headercon.fill = GridBagConstraints.HORIZONTAL;
        headercon.anchor = GridBagConstraints.FIRST_LINE_START;
        contents.add(header, headercon);

        GridBagConstraints sidebarcon = new GridBagConstraints();
        sidebarcon.gridx = 0;
        sidebarcon.gridy = 1;
        sidebarcon.weighty = 1;
        sidebarcon.fill = GridBagConstraints.VERTICAL;
        sidebarcon.anchor = GridBagConstraints.FIRST_LINE_START;
        contents.add(sidebar, sidebarcon);

        GridBagConstraints complareacon = new GridBagConstraints();
        complareacon.gridx = 1;
        complareacon.gridy = 1;
        complareacon.weightx = 1;
        complareacon.weighty = 1;
        complareacon.fill = GridBagConstraints.BOTH;
        complareacon.anchor = GridBagConstraints.FIRST_LINE_START;
        contents.add(complarea, complareacon);


        GridBagConstraints contentscon = new GridBagConstraints();
        contentscon.gridx = 0;
        contentscon.gridy = 0;
        contentscon.weightx = 1;
        contentscon.weighty = 1;
        contentscon.fill = GridBagConstraints.BOTH;
        contentscon.anchor = GridBagConstraints.FIRST_LINE_START;
        mainframe.add(contents, contentscon);

        mainframe.pack();
        this.prefix_input.requestFocusInWindow();
        mainframe.setVisible(true);

        String corpus_action = "";
        for (Map.Entry<String, String> entry : this.default_corpora.entrySet()) {
            if (dataset_path.equals(entry.getValue())) {
                corpus_action = entry.getKey();
                break;
            }
        }

        switch (corpus_action) {
            case "corpus-large":
                corplarg.setSelected(true);
                break;
            case "corpus-medium":
                corpmedm.setSelected(true);
                break;
            case "corpus-small":
                corpsmal.setSelected(true);
                break;
            case "corpus-tiny":
                corptiny.setSelected(true);
                break;
            case "corpus-test":
                corptest.setSelected(true);
                break;
            default:
                this.corpus_input.setText(dataset_path);
        }
    }

    public void actionPerformed(ActionEvent evt) {
        String command = evt.getActionCommand();
        if (command.startsWith("corpus-")) {
            handleCorpusChange(command);
        }
        else if (command.equals("word-add")) {
            handleWordAdd();
        }
        else if (command.equals("word-lookup")) {
            handleWordLookup();
        }
    }

    public void stateChanged(ChangeEvent evt) {
        if (evt.getSource() == this.max_error_input) {
            handleMaxErrorChange();
        }
        else if (evt.getSource() == this.num_results_input) {
            handleNumResultsChange();
        }
    }

    public void changedUpdate(DocumentEvent evt) {
        // Don't care; do nothing.
    }

    public void insertUpdate(DocumentEvent evt) {
        handleTextUpdate(evt.getOffset());
    }

    public void removeUpdate(DocumentEvent evt) {
        handleTextUpdate(evt.getOffset());
    }

    private void handleWordAdd() {
        String word_to_add = this.add_word_input.getText();
        Trie trie = this.autocompleter.getTrie();
        trie.addWord(word_to_add);
        this.autocompleter = Autocompleter.build(trie, this.autocompleter.getMaxError());
        addWholePrefix();
        displayCompletions();
        this.add_word_input.setText("");
    }

    private void handleWordLookup() {
        String word_to_look = this.look_word_input.getText();
        int word_freq = this.autocompleter.getTrie().lookupWord(word_to_look);
        this.look_word_output.setText("\"" + word_to_look + "\" has a frequency of " + Integer.toString(word_freq));
    }

    private void handleCorpusChange(String corpus) {
        String corpus_path;

        if (corpus.equals("corpus-custom")) {
            corpus_path = this.corpus_input.getText();
        }
        else {
            corpus_path = this.default_corpora.get(corpus);
        }

        Trie new_trie = Trie.fromFile(corpus_path);

        if (new_trie == null) {
            JOptionPane.showMessageDialog(this.main_frame, "That filepath is invalid.");
            this.corpus_input.setText("");
            return;
        }

        this.autocompleter = Autocompleter.build(new_trie, this.autocompleter.getMaxError());
        addWholePrefix();
        displayCompletions();
        handleWordLookup();

        if (corpus.equals("corpus-custom")) {
            this.corpus_btn_group.clearSelection();
        }
        else {
            this.corpus_input.setText("");
        }
    }

    private void handleMaxErrorChange() {
        int new_max_error = (Integer)this.max_error_input.getModel().getValue();
        this.autocompleter = Autocompleter.build(this.autocompleter.getTrie(), new_max_error);
        addWholePrefix();
        displayCompletions();
    }

    private void handleNumResultsChange() {
        int new_num_results = (Integer)this.num_results_input.getModel().getValue();
        this.completion_output.setVisibleRowCount(new_num_results);
        displayCompletions();
    }

    private void handleTextUpdate(int offset) {
        String new_text = this.prefix_input.getText();
        String added_text = new_text.substring(offset);
        String removed_text = this.old_text.substring(offset);
        this.old_text = new_text;
        for (int idx = 0; idx < removed_text.length(); idx++) {
            this.autocompleter.delChar();
        }
        for (int idx = 0; idx < added_text.length(); idx++) {
            this.autocompleter.addChar(added_text.charAt(idx));
        }
        displayCompletions();
    }

    private void addWholePrefix() {
        String prefix = this.prefix_input.getText();
        for (int idx = 0; idx < prefix.length(); idx++) {
            this.autocompleter.addChar(prefix.charAt(idx));
        }
    }

    private void displayCompletions() {
        int num_results = this.completion_output.getVisibleRowCount();
        setCompletions(this.autocompleter.getWords(num_results));
    }

    private void setCompletions(List<String> completions) {
        DefaultListModel listmodel = (DefaultListModel)this.completion_output.getModel();
        listmodel.clear();
        for (String completion : completions) {
            listmodel.addElement(completion);
        }
    }
}