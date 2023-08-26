from functools import wraps

from flask import abort
from flask_login import current_user

ROLES = {
    'Guest': 0,
    'Moderator': 1,
    'Administrator': 2
}


def role_required(required_role):
    def decorator(func):
        @wraps(func)
        def wrapper(*args, **kwargs):
            if not has_access(required_role):
                # No permission to access route
                abort(403)
            return func(*args, **kwargs)
        return wrapper
    return decorator


def has_access(required_role):
    return ROLES.get(current_user.role, 0) >= ROLES.get(required_role, 100)
