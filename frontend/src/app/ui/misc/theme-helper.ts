import { CleanupJsonString } from "@common/forms/validation/custom-validator";
import { argbFromHex, themeFromSourceColor, applyTheme } from "@material/material-color-utilities";

const FONT_VARS = [
    "--sys-body-large-size",
    "--sys-body-medium-size",
    "--sys-body-small-size",
    "--sys-display-large-size",
    "--sys-display-medium-size",
    "--sys-display-small-size",
    "--sys-headline-large-size",
    "--sys-headline-medium-size",
    "--sys-headline-small-size",
    "--sys-label-large-size",
    "--sys-label-medium-size",
    "--sys-label-small-size",
    "--sys-title-large-size",
    "--sys-title-medium-size",
    "--sys-title-small-size",
    "--mat-standard-button-toggle-label-text-size"
]

export function generateDynamicTheme(primaryColor: string) {
    const fallbackPrimary = '#18488F';

    let argbPrimary;
    try {
        argbPrimary = argbFromHex(primaryColor);
    } catch (error) {
        // falling to default color if it's invalid color
        argbPrimary = argbFromHex(fallbackPrimary);
    }

    const targetElement = document.documentElement;

    // Get the theme from a hex color
    const theme = themeFromSourceColor(argbPrimary);

    // Apply theme to root element
    applyTheme(theme, {
      target: targetElement,
      dark: false,
      brightnessSuffix: true,
    });

    const styles = targetElement.style;

    for (const key in styles) {
      if (Object.prototype.hasOwnProperty.call(styles, key)) {
        const propName = styles[key];
        if (propName.indexOf('--md-sys') === 0) {
          const sysPropName = '--sys' + propName.replace('--md-sys-color', '');
          targetElement.style.setProperty(
            sysPropName,
            targetElement.style.getPropertyValue(propName)
          );
        }
      }
    }
}

export function overrideCss(input: string){
    let json;
    const targetElement = document.documentElement;
    try {
        const value = CleanupJsonString(input);
        json = JSON.parse(value);
    } catch(error) {
        return;
    }
    for (const [key, rgba] of Object.entries(json)) {
        if (key.indexOf('--md-sys') === 0) {
          const sysPropName = '--sys' + key.replace('--md-sys-color', '');
          targetElement.style.setProperty(
            sysPropName,
            rgba.toString()
          );
        }
        targetElement.style.setProperty(
            key,
            rgba.toString()
          );
    }
}

export function toggleFontSize(large: boolean){
    if(large){
        FONT_VARS.forEach((key) => {
            const prevValue = getComputedStyle(document.body).getPropertyValue(key);
            const newValue = Number(prevValue.replace('rem', '')) * 1.085;
            document.documentElement.style.setProperty(
                key,
                `${newValue}rem`
            )
        })
    } else {
       FONT_VARS.forEach((key) => {
        const prevValue = getComputedStyle(document.body).getPropertyValue(key);
        const newValue = Number(prevValue.replace('rem', '')) / 1.085;
        document.documentElement.style.setProperty(
            key,
            `${newValue}rem`
        )
       })     
    }
}

interface ColorData {
    tone: number;
    hex: string;
}