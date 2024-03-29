"""Initial migration.

Revision ID: 01ff901f94db
Revises: 
Create Date: 2023-08-26 23:39:25.254591

"""
from alembic import op
import sqlalchemy as sa


# revision identifiers, used by Alembic.
revision = '01ff901f94db'
down_revision = None
branch_labels = None
depends_on = None


def upgrade():
    # ### commands auto generated by Alembic - please adjust! ###
    op.create_table('settings',
    sa.Column('key', sa.String(), nullable=False),
    sa.Column('value', sa.String(), nullable=True),
    sa.PrimaryKeyConstraint('key')
    )
    op.create_table('users',
    sa.Column('id', sa.Integer(), nullable=False),
    sa.Column('email', sa.String(length=80), nullable=True),
    sa.Column('username', sa.String(length=20), nullable=True),
    sa.Column('password_hash', sa.String(), nullable=True),
    sa.PrimaryKeyConstraint('id'),
    sa.UniqueConstraint('email'),
    sa.UniqueConstraint('username')
    )
    op.create_table('moderated_binaries',
    sa.Column('checksum', sa.String(), nullable=False),
    sa.Column('app_slug', sa.String(), nullable=False),
    sa.Column('status', sa.String(), nullable=False),
    sa.Column('discovery_date', sa.DateTime(), nullable=False),
    sa.Column('modified_date', sa.DateTime(), nullable=False),
    sa.Column('moderated_by', sa.Integer(), nullable=True),
    sa.ForeignKeyConstraint(['moderated_by'], ['users.id'], ),
    sa.PrimaryKeyConstraint('checksum')
    )
    # ### end Alembic commands ###


def downgrade():
    # ### commands auto generated by Alembic - please adjust! ###
    op.drop_table('moderated_binaries')
    op.drop_table('users')
    op.drop_table('settings')
    # ### end Alembic commands ###
