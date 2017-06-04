package amidst.gui.searchinseed;

import amidst.AmidstMetaData;
import amidst.documentation.AmidstThread;
import amidst.documentation.CalledOnlyBy;
import amidst.documentation.NotThreadSafe;
import amidst.gui.main.MainWindowDialogs;
import amidst.gui.main.WorldSwitcher;
import amidst.gui.searchinseed.SeedInSearcher;
import amidst.mojangapi.world.WorldType;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;

@NotThreadSafe
public class SearchInSeedWindow {

    private final AmidstMetaData metadata;
    private final MainWindowDialogs dialogs;
    private final WorldSwitcher worldSwitcher;
    //private final SeedSearcher seedSearcher;

    private final JTextArea searchQueryTextArea;
    private final JComboBox<WorldType> worldTypeComboBox;
    private final JCheckBox searchContinuouslyCheckBox;
    private final JButton searchButton;
    private final JFrame frame;

    @CalledOnlyBy(AmidstThread.EDT)
    public SearchInSeedWindow(
            AmidstMetaData metadata,
            MainWindowDialogs dialogs,
            WorldSwitcher worldSwitcher,
            SeedInSearcher seedSearcher) {
        this.metadata = metadata;
        this.dialogs = dialogs;
        this.worldSwitcher = worldSwitcher;
        //this.seedSearcher = seedSearcher;
        this.searchQueryTextArea = createSearchQueryTextArea();
        this.worldTypeComboBox = createWorldTypeComboBox();
        this.searchContinuouslyCheckBox = createSearchContinuouslyCheckBox();
        this.searchButton = createSearchButton();
        this.frame = createFrame();
    }

    @CalledOnlyBy(AmidstThread.EDT)
    public void show() {
        this.frame.setVisible(true);
        updateGUI();
    }

    @CalledOnlyBy(AmidstThread.EDT)
    private JTextArea createSearchQueryTextArea() {
        return new JTextArea();
    }

    @CalledOnlyBy(AmidstThread.EDT)
    private JComboBox<WorldType> createWorldTypeComboBox() {
        return new JComboBox<>(WorldType.getSelectableArray());
    }

    @CalledOnlyBy(AmidstThread.EDT)
    private JCheckBox createSearchContinuouslyCheckBox() {
        return new JCheckBox("search continuously");
    }

    @CalledOnlyBy(AmidstThread.EDT)
    private JButton createSearchButton() {
        JButton result = new JButton("Search");
        result.addActionListener(e -> searchButtonClicked());
        return result;
    }

    @CalledOnlyBy(AmidstThread.EDT)
    private JFrame createFrame() {
        JFrame result = new JFrame("Seed Searcher");
        result.setIconImages(metadata.getIcons());
        result.getContentPane().setLayout(new MigLayout());
        result.add(new JLabel("Search Query:"), "growx, pushx, wrap");
        result.add(createScrollPane(searchQueryTextArea), "grow, push, wrap");
        result.add(new JLabel("World Type:"), "growx, pushx, wrap");
        result.add(worldTypeComboBox, "growx, pushx, wrap");
        result.add(searchContinuouslyCheckBox, "growx, pushx, wrap");
        result.add(searchButton, "pushx, wrap");
        result.setSize(800, 600);
        result.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        return result;
    }

    @CalledOnlyBy(AmidstThread.EDT)
    private JScrollPane createScrollPane(JTextArea textArea) {
        JScrollPane result = new JScrollPane(textArea);
        result.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        result.setBorder(new LineBorder(Color.darkGray, 1));
        return result;
    }

    @CalledOnlyBy(AmidstThread.EDT)
    private void searchButtonClicked() {
        /*if (seedSearcher.isSearching()) {
            seedSearcher.stop();
        } else {
            Optional<SeedSearcherConfiguration> configuration = createSeedSearcherConfiguration();
            if (configuration.isPresent()) {
                SeedSearcherConfiguration seedSearcherConfiguration = configuration.get();
                WorldType worldType = seedSearcherConfiguration.getWorldType();
                seedSearcher.search(seedSearcherConfiguration, worldSeed -> seedFound(worldSeed, worldType));
            } else {
                AmidstLogger.warn("invalid configuration");
                dialogs.displayError("invalid configuration");
            }
        }*/
        updateGUI();
    }

    /*
    @CalledOnlyBy(AmidstThread.EDT)
    private Optional<SeedSearcherConfiguration> createSeedSearcherConfiguration() {
        return WorldFilterJson_MatchAll
                .from(searchQueryTextArea.getText())
                .flatMap(WorldFilterJson_MatchAll::createValidWorldFilter)
                .map(this::createSeedSearcherConfiguration);
    }

    @CalledOnlyBy(AmidstThread.EDT)
    private SeedSearcherConfiguration createSeedSearcherConfiguration(WorldFilter worldFilter) {
        return new SeedSearcherConfiguration(
                worldFilter,
                (WorldType) worldTypeComboBox.getSelectedItem(),
                searchContinuouslyCheckBox.isSelected());
    }

    @CalledOnlyBy(AmidstThread.EDT)
    private void seedFound(WorldSeed worldSeed, WorldType worldType) {
        worldSwitcher.displayWorld(worldSeed, worldType);
        updateGUI();
    }*/

    @CalledOnlyBy(AmidstThread.EDT)
    private void updateGUI() {
        if(false){//if (seedSearcher.isSearching() && !seedSearcher.isStopRequested()) {
            searchButton.setText("Stop");
            searchQueryTextArea.setEditable(false);
            worldTypeComboBox.setEnabled(false);
            searchContinuouslyCheckBox.setEnabled(false);
        } else {
            searchButton.setText("Search");
            searchQueryTextArea.setEditable(true);
            worldTypeComboBox.setEnabled(true);
            searchContinuouslyCheckBox.setEnabled(true);
        }
    }

    @CalledOnlyBy(AmidstThread.EDT)
    public void dispose() {
    }//seedSearcher.dispose();
}
