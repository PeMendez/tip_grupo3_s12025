import React, { createContext, useContext, useState } from "react";

const TitleContext = createContext(undefined);

export const TitleProvider = ({ children }) => {
    const [headerTitle, setHeaderTitle] = useState("NeoHub");

    return (
        <TitleContext.Provider value={{ headerTitle, setHeaderTitle}}>
            {children}
        </TitleContext.Provider>
    );
};

export const useTitle = () => useContext(TitleContext);

