"""Add reference information to countries.

Revision ID: c421e8a76b35
Revises: 07d72dc8df72
"""
from typing import Sequence, Union

from alembic import op
import sqlalchemy as sa


revision: str = "c421e8a76b35"
down_revision: Union[str, Sequence[str], None] = "07d72dc8df72"
branch_labels: Union[str, Sequence[str], None] = None
depends_on: Union[str, Sequence[str], None] = None


def upgrade() -> None:
    op.add_column("countries", sa.Column("population", sa.BigInteger(), nullable=True))
    op.add_column("countries", sa.Column("languages", sa.JSON(), nullable=True))
    op.add_column("countries", sa.Column("timezone", sa.String(length=100), nullable=True))


def downgrade() -> None:
    op.drop_column("countries", "timezone")
    op.drop_column("countries", "languages")
    op.drop_column("countries", "population")
