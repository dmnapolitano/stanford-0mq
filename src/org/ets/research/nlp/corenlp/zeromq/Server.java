package org.ets.research.nlp.corenlp.zeromq;

import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.Socket;

import edu.stanford.nlp.parser.shiftreduce.ShiftReduceParser;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;

public class Server
{
    private final int THREADS = 10;
    private ShiftReduceParser srModel;
    private MaxentTagger tagger;

    public Server(String myBrokerAddress)
    {
        try
        {
            System.err.println("Initializing MaxentTagger...");
            tagger = new MaxentTagger("edu/stanford/nlp/models/pos-tagger/english-left3words/english-left3words-distsim.tagger");

            System.err.println("Initializing Shift-Reduce Parser...");
            srModel = ShiftReduceParser.loadModel("edu/stanford/nlp/models/srparser/englishSR.ser.gz");

            Context context = ZMQ.context(1);

            Socket clients = context.socket(ZMQ.ROUTER);
            clients.bind(myBrokerAddress);

            Socket workers = context.socket(ZMQ.DEALER);
            workers.bind("inproc://workers");

            for (int thread_nbr = 0; thread_nbr < this.THREADS; thread_nbr++)
            {
                System.err.println("Starting worker thread " + thread_nbr);
                Thread worker = new Worker(context, srModel, tagger);
                worker.start();
            }

            // Connect work threads to client threads via a queue
            ZMQ.proxy (clients, workers, null);
        }
        catch (Exception e)
        {
            // TODO: Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void main(String[] args)
    {
        // org.ets.research.nlp.corenlp.zeromq.Server <broker address>
        // for example:
        // tcp://127.0.0.1:5555
        @SuppressWarnings("unused")
        Server server = new Server(args[0]);
    }
}