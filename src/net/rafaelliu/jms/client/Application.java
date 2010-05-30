/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * NewJFrame.java
 *
 * Created on May 8, 2010, 9:26:27 AM
 */

package net.rafaelliu.jms.client;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.InitialContext;

public class Application extends javax.swing.JFrame implements MessageListener {
	private static final long serialVersionUID = 1L;
	
	private String jndiUrl, jmsConnFactory, jmsDestination;
	private SimpleDateFormat pdf = new SimpleDateFormat("[HH:mm:ss]");

	/**
	 * Example: args = [127.0.0.1:1100, ClusteredConnectionFactory, queue/testDistributedQueue ]
	 */
    public Application(String jndiUrl, String jmsConnFactory, String jmsDestination) {
	    this.jndiUrl = jndiUrl;
	    this.jmsConnFactory = jmsConnFactory;
	    this.jmsDestination = jmsDestination;

        initComponents();
        initMessageListener();
    }

	/**
	 * Swing stuff
	 */

    private javax.swing.JButton btnClear;
    private javax.swing.JButton btnPause;
    private javax.swing.JButton btnClose;
    private javax.swing.JPanel panel;
    private javax.swing.JScrollPane scrollPane;
    private javax.swing.JTextArea textArea;
	private boolean paused = false;

	
    private void initComponents() {
        scrollPane = new javax.swing.JScrollPane();
        textArea = new javax.swing.JTextArea();
        panel = new javax.swing.JPanel();
        btnClear = new javax.swing.JButton();
        btnPause = new javax.swing.JButton();
        btnClose = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle(jndiUrl + " - " + jmsConnFactory + " - " + jmsDestination);

        textArea.setColumns(80);
        textArea.setRows(15);
        textArea.setEditable(false);
        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        scrollPane.setViewportView(textArea);

        getContentPane().add(scrollPane, java.awt.BorderLayout.CENTER);

        btnPause.setText("Pause");
        btnPause.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
                    btnPauseActionPerformed(evt);
				} catch (JMSException e) {
					textArea.append(e.getMessage() + "\n");
				}
            }
        });
        panel.add(btnPause);

        btnClear.setText("Clear");
        btnClear.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnClearActionPerformed(evt);
            }
        });
        panel.add(btnClear);

        btnClose.setText("Close");
        btnClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                try {
					btnCloseActionPerformed(evt);
				} catch (JMSException e) {
					textArea.append(e.getMessage() + "\n");
				}
            }
        });
        panel.add(btnClose);

        getContentPane().add(panel, java.awt.BorderLayout.PAGE_END);

        pack();
    }
    
    protected void btnClearActionPerformed(ActionEvent evt) {
    	textArea.setText("");
	}

    protected void btnPauseActionPerformed(ActionEvent evt) throws JMSException {
		if (paused = !paused) {
			receiver.setMessageListener(null);
			btnPause.setText("Start");
		} else {
			receiver.setMessageListener(this);
			btnPause.setText("Pause");
		}
	}

	protected void btnCloseActionPerformed(ActionEvent evt) throws JMSException {
        session.close();
        conn.close();
    	System.exit(0);
	}
	
	
	
	
	/**
	 * JMS stuff
	 */

    private ConnectionFactory connFactory;
    private Connection conn;
    private Session session;
    private MessageConsumer receiver;
    private Destination destination;

    private void initMessageListener() {
        try {
        	Properties props = new Properties();
        	props.put("java.naming.factory.initial", "org.jnp.interfaces.NamingContextFactory");
        	props.put("java.naming.factory.url.pkgs", "org.jboss.naming:org.jnp.interfaces");
			props.put("java.naming.provider.url", jndiUrl);

            InitialContext ctx = new InitialContext(props);
			connFactory = (ConnectionFactory) ctx.lookup(jmsConnFactory);
			destination = (Destination) ctx.lookup(jmsDestination);
            conn = connFactory.createConnection();
            session = conn.createSession(false, QueueSession.AUTO_ACKNOWLEDGE);
            receiver = session.createConsumer(destination);
            receiver.setMessageListener(this);
            conn.start();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    public void onMessage(Message message) {
        TextMessage textMessage = (TextMessage) message;
        try {
        	textArea.append(pdf.format(new Date()) + " " + textMessage.getText() + "\n");
        } catch (JMSException e) {
        	textArea.append(e.getMessage() + "\n");
        }
    }

    public static void main(final String[] args) {
        if (args.length != 3) {
        	System.out.println("PARAMETERS: <JNDI URL:PORT> <JMS CONNECTION FACTORY> <JMS DESTINATION>");
        	System.exit(1);
        }

        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Application(args[0], args[1], args[2]).setVisible(true);
            }
        });
    }

}
