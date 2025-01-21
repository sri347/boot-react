import React, { useState } from 'react';
import axios from 'axios';

function App() {
  const [streamedResponse, setStreamedResponse] = useState('');
  const [loading, setLoading] = useState(false);

  // Function to handle API call and stream response
  const handleApiCall = async (apiUrl) => {
    setStreamedResponse(''); // Clear previous response
    setLoading(true); // Show loading
  
    try {
      // Use fetch API directly for better control over streaming
      const response = await fetch(apiUrl);
  
      // Check if the response is ok
      if (response.ok) {
        const reader = response.body.getReader();
        const decoder = new TextDecoder();
        let done = false;
        let result = '';
  
        // Read the response body in chunks and process each chunk
        while (!done) {
          const { value, done: doneReading } = await reader.read();
          done = doneReading;
  
          // Decode the chunk and append it to the response state
          result += decoder.decode(value, { stream: true });
  
          // Update UI with the chunked data
          setStreamedResponse((prev) => prev + result);
          result = '';  // Reset the result to prepare for the next chunk
        }
      } else {
        console.error("Failed to fetch streaming data");
      }
    } catch (error) {
      console.error("Error while calling API:", error);
    } finally {
      setLoading(false); // Hide loading when done
    }
  };  

  return (
    <div className="App">
      <h1>Streaming API Response</h1>
      
      <div>
        {/* Buttons to trigger API calls */}
        <button onClick={() => handleApiCall('http://localhost:8090/stream-json')} style={{ margin: '10px' }}>
          Call /stream-json API
        </button>


        <button onClick={() => handleApiCall('http://localhost:8090/consume-stream')} style={{ margin: '10px' }}>
          Call /consume-stream API
        </button>


        <button onClick={() => handleApiCall('http://localhost:8090/consume-streamv2')} style={{ margin: '10px' }}>
          Call /stream-json API V2
        </button>
      </div>
      
      {/* Text area to visualize the streamed response */}
      <div>
        <h3>Streamed Response</h3>
        <textarea
          value={streamedResponse}
          rows="10"
          cols="50"
          readOnly
          style={{ whiteSpace: 'pre-wrap' }}
        />
      </div>

      {loading && <p>Loading...</p>}
    </div>
  );
}

export default App;
