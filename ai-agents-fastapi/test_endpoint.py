#!/usr/bin/env python3

import requests
import json
import sys

def test_endpoint():
    """Test the sponsor recommendation endpoint"""
    url = "http://localhost:8001/api/v1/sponsor-recommendation"
    
    try:
        print("Testing endpoint:", url)
        response = requests.get(url)
        
        print("Status Code:", response.status_code)
        print("Response Headers:", dict(response.headers))
        
        if response.status_code == 200:
            data = response.json()
            print("\nResponse Data:")
            print(json.dumps(data, indent=2))
        else:
            print("Error Response:")
            print(response.text)
            
    except requests.exceptions.ConnectionError:
        print("Error: Could not connect to the server. Make sure it's running on port 8001")
    except Exception as e:
        print(f"Error: {e}")

if __name__ == "__main__":
    test_endpoint()
