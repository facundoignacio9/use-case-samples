package com.company.finance;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.browser.ProgressListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import com.equo.chromium.swt.Browser;

public class EquoFinanceDashboard {

    private static class Company {
        String name;
        String ticker;
        int sharesOwned;
        double sharePrice;
        String homepage;

        Company(String name, String ticker, int sharesOwned, double sharePrice, String homepage) {
            this.name = name;
            this.ticker = ticker;
            this.sharesOwned = sharesOwned;
            this.sharePrice = sharePrice;
            this.homepage = homepage;
        }

        String totalValue() {
            return "$" + String.format("%,.2f", sharesOwned * sharePrice);
        }
    }

    private static final Company[] SAMPLE_DATA = new Company[]{
        new Company("Coca-Cola", "KO", 150, 61.20, "https://www.coca-colacompany.com/"),
        new Company("Apple", "AAPL", 20, 210.50, "https://www.apple.com/"),
        new Company("Microsoft", "MSFT", 10, 415.30, "https://www.microsoft.com/"),
        new Company("NVIDIA", "NVDA", 8, 1230.10, "https://www.nvidia.com/")
    };

    public static void main(String[] args) {

        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("Company Portfolio Dashboard");
        shell.setSize(1000, 750);
        shell.setLayout(new GridLayout());

        Composite topPanel = new Composite(shell, SWT.NONE);
        topPanel.setLayout(new GridLayout(2, false));
        topPanel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        Table table = new Table(topPanel, SWT.FULL_SELECTION);
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        String[] titles = {"Company", "Ticker", "Shares", "Total Value"};
        for (String title : titles) {
            TableColumn col = new TableColumn(table, SWT.NONE);
            col.setText(title);
            col.setWidth(160);
        }

        for (Company c : SAMPLE_DATA) {
            TableItem item = new TableItem(table, SWT.NONE);
            item.setText(new String[]{
                c.name,
                c.ticker,
                Integer.toString(c.sharesOwned),
                c.totalValue()
            });
        }

        Composite rightPanel = new Composite(topPanel, SWT.NONE);
        rightPanel.setLayout(new GridLayout(1, false));
        rightPanel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        Label title = new Label(rightPanel, SWT.NONE);
        title.setText("Company Details");
        title.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

        Text detailsBox = new Text(rightPanel, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
        detailsBox.setEditable(false);
        detailsBox.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        Button btnStockChart = new Button(rightPanel, SWT.PUSH);
        btnStockChart.setText("Show Stock Price Chart");
        btnStockChart.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        Button btnOpenWebsite = new Button(rightPanel, SWT.PUSH);
        btnOpenWebsite.setText("Open Company Website");
        btnOpenWebsite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        Composite browserPanel = new Composite(shell, SWT.NONE);
        browserPanel.setLayout(new GridLayout());
        browserPanel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        Browser browser = new Browser(browserPanel, SWT.NONE);
        browser.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        table.addListener(SWT.Selection, e -> {
            TableItem item = (TableItem) e.item;
            String name = item.getText(0);
            String ticker = item.getText(1);
            String shares = item.getText(2);
            String totalValue = item.getText(3);

            detailsBox.setText(
                "Company: " + name + "\n" +
                "Ticker: " + ticker + "\n" +
                "Shares Owned: " + shares + "\n" +
                "Total Value: " + totalValue + "\n\n" +
                "Notes:\n- Click a button to view more info.\n"
            );
        });

        btnStockChart.addListener(SWT.Selection, e -> {
            TableItem selected = table.getSelectionCount() > 0 ? table.getSelection()[0] : null;    
            if (selected != null) {
                String ticker = selected.getText(1);

                String chartUrl =
                    "https://query1.finance.yahoo.com/v7/finance/chart/" + ticker +
                    "?range=1mo&interval=1d";

                browser.setUrl("https://finance.yahoo.com/quote/" + ticker + "/chart");
            }
        });

        btnOpenWebsite.addListener(SWT.Selection, e -> {
            TableItem selected = table.getSelectionCount() > 0 ? table.getSelection()[0] : null;
            if (selected != null) {
                String companyName = selected.getText(0);
                Company company = null;
                for (Company c : SAMPLE_DATA) {
                    if (c.name.equals(companyName)) { company = c; break; }
                }
                if (company != null) {
                    browser.setUrl(company.homepage);
                }
            }
        });

        browser.addProgressListener(new ProgressListener() {
            @Override
            public void completed(ProgressEvent event) {
                browser.evaluate("document.body.style.zoom='60%'");
            }
            @Override
            public void changed(ProgressEvent event) {
            }
        });

        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) display.sleep();
        }
        display.dispose();
    }
}
