package net.rafaelliu.jms.producer;


import java.util.Properties;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.InitialContext;

public class JmsProducer {

	/**
	 * Example: args = [ "127.0.0.1:1100,127.0.0.1:1200", ClusteredConnectionFactory, queue/testDistributedQueue, 10, 1, "Test Message" ]
	 */
	public static void main(String[] args) throws Exception {
        if (args.length != 6) {
        	System.out.println("PARAMETERS: <JNDI URL:PORT> <JMS CONNECTION FACTORY> <JMS DESTINATION> <# THREADS> <# ITERATIONS> <MESSAGE>");
        	System.exit(1);
        }
        
		new JmsProducer("127.0.0.1:1100,127.0.0.1:1200", args[1], args[2]).produce(Integer.parseInt(args[3]), Integer.parseInt(args[4]), args[5]);
	}

	private String jndiUrl, jmsConnFactory, jmsDestination;

	public JmsProducer(String jndiUrl, String jmsConnFactory, String jmsDestination) {
	    this.jndiUrl = jndiUrl;
	    this.jmsConnFactory = jmsConnFactory;
	    this.jmsDestination = jmsDestination;
	}


	
	/**
	 * Start <i>nthreads</i> threads and run <i>ntimes</i> each thread sending 
	 * <i>message</i> as a text message to <i>destination</i>
	 */
	public void produce(int nthreads, int ntimes, String message) throws Exception {
    	Properties props = new Properties();
    	props.put("java.naming.factory.initial", "org.jnp.interfaces.NamingContextFactory");
    	props.put("java.naming.factory.url.pkgs", "org.jboss.naming:org.jnp.interfaces");
		props.put("java.naming.provider.url", jndiUrl);

		InitialContext iniCtx = new InitialContext(props);
		ConnectionFactory cf = (ConnectionFactory) iniCtx.lookup(jmsConnFactory);
		Destination d = (Destination) iniCtx.lookup(jmsDestination);

		for (int i = 0; i < nthreads; i++) {
			ProducerThread pt = new ProducerThread(cf, d, message, ntimes);
			pt.start();
			pt.join();
		}

		System.out.println("All done!");
	}

	private class ProducerThread extends Thread {

		private ConnectionFactory connectionFactory;
		private Destination destination;
		private int times;
		private String message;

		public ProducerThread(ConnectionFactory connectionFactory, Destination destination, String message, int times) throws JMSException {
			this.connectionFactory = connectionFactory;
			this.destination = destination;
			this.message = message;
			this.times = times;
		}

		@Override
		public void run() {

			Connection conn = null;
			Session session = null;
			try {
				conn = connectionFactory.createConnection();
				session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
				System.out.println(Thread.currentThread().getId() + " " + conn);

				String text = "thread " + Thread.currentThread().getId() + " sent: " + message;
				TextMessage textMessage = session.createTextMessage(text);
				MessageProducer producer = session.createProducer(destination);
				for (int i = 0; i < times; i ++) {
					producer.send(textMessage);
				}
				producer.close();

			} catch (Exception e) {
				throw new RuntimeException(e);
			} finally {
				try {
					session.close();
					conn.close();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}

		}

	}

}
