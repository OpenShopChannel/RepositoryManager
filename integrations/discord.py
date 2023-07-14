import requests
from flask import url_for

import config


def send_webhook_message(url, title, description, username="Repository Manager", message=""):
    if config.ENABLE_DISCORD_WEBHOOKS:
        try:
            data = {"content": message,
                    "username": username,
                    "embeds": [
                        {
                            "description": description,
                            "title": title
                        }
                    ]
                    }

            requests.post(url, json=data)
        except Exception as e:
            print("Could not send a discord webhook.", e)
