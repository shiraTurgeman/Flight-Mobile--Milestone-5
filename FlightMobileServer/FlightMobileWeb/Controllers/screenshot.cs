using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Net;
using System.Net.Http;
using System.Text;
using System.Threading.Tasks;
using FlightMobileWeb.Model;
using Microsoft.AspNetCore.Mvc;
using Microsoft.Extensions.Configuration;

namespace FlightMobileWeb.Controllers
{
    [Route("[controller]")]
    [ApiController]
    public class screenshot : ControllerBase
    {
        IConfiguration configuration;

        public screenshot(IConfiguration configuration)
        {
            this.configuration = configuration;

        }

        // GET: api/<screenshot>
        [HttpGet]
        public async Task<System.IO.Stream> Get()
        {
            HttpClient client = new HttpClient();
            string imagePort = configuration.GetValue<string>("flightGearImagePort");
            string imageIP = configuration.GetValue<string>("flightGearImageIP");
            string url = "http://" + imageIP + ":" + imagePort + "/screenshot";
            try
            {
                HttpResponseMessage response = await client.GetAsync(url);
                response.EnsureSuccessStatusCode();
                System.IO.Stream responseBody = await response.Content.ReadAsStreamAsync();
                if (response.IsSuccessStatusCode)
                {
                    return responseBody;
                }
                else
                {
                    Response.StatusCode = 400;
                    client.Dispose();
                    return null;
                }

            }
            catch 
            {
                Response.StatusCode = 400;
                client.Dispose();
                return null;
            }
        }
    }
}
