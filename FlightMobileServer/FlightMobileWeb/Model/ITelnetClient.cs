using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;

namespace FlightMobileWeb.Model
{
    //The interface that define the communication between the model and the server.
    public interface ITelnetClient
    {
        //function for the communication between the model and the server.
        void connect(string ip, int port);
        void write(string command);
        string read();
        void disconnect();
    }
}
