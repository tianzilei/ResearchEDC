from pydantic import BaseModel, ConfigDict


class PaginationParams(BaseModel):
    page: int = 1
    page_size: int = 20


class PaginatedResponse(BaseModel):
    items: list
    total: int
    page: int
    page_size: int

    model_config = ConfigDict(arbitrary_types_allowed=True)
