using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Net.Sockets;
using System.Threading.Tasks;

namespace FlightMobileWeb.Model
{
    //the class that implement the telnet client interface.
    public class TelnetClient:ITelnetClient
    {

        //variables for the project.
        public TcpClient client;

        //this function connects the model to the server.
        public void connect(string ip, int port)
        {
            client = new TcpClient();
            client.Connect(ip, port);
        }
        //this function writes the message to the server.
        public void write(string command)
        {

            Byte[] data = System.Text.Encoding.ASCII.GetBytes(command);
            client.GetStream().Write(data, 0, data.Length);

        }
        //this function read the values from the simulator.
        public string read()
        {
            StreamReader reader2 = new StreamReader(client.GetStream());
            string buff = reader2.ReadLine();
            return buff;
        }
        //this function disconnect the model to the server.
        public void disconnect()
        {
            if (client == null)
            {
                Console.WriteLine("Client not connected- can't disconnect");
                return;
            }
            client.Close();
            client = null;
        }
    }
}
